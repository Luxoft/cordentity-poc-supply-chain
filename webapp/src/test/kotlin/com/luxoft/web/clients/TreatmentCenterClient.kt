/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.luxoft.web.clients

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialValue
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.Invite
import com.luxoft.web.data.PackagesResponse
import com.luxoft.web.data.Serial
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

enum class TreatmentCenterEndpoint(val url: String) {
    INVITE("/api/tc/invite"),
    LIST("/api/tc/package/list"),
    INIT("/api/tc/request/create"),
    RECEIVE("/api/tc/package/receive"),
    COLLECT("/api/tc/package/withdraw"),
    HISTORY("/api/tc/package/history"),
}

@Component
class TreatmentCenterClient {
    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    lateinit var ssiUser: SsiUser

    @Autowired
    lateinit var agentClient: AgentConnection

    @Value("\${treatmentCenterEndpoint}")
    lateinit var endpoint: String

    private lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        restTemplate = restTemplateBuilder.rootUri(endpoint).build()
    }

    fun getPackages(): PackagesResponse = this.restTemplate.getForObject(TreatmentCenterEndpoint.LIST.url)
        ?: throw RuntimeException("Failed to request packages")

    fun getInvite(): Invite =
        restTemplate.getForObject(TreatmentCenterEndpoint.INVITE.url) ?: throw RuntimeException("Failed to get invite")

    //Just in case of need
    fun createIndyPrerequirements(indyUser: IndyUser) = indyUser.apply {
        val requiredAttributes = mutableListOf("name", "sex", "medical id", "medical condition", "age")
        val schema = createSchemaAndStoreOnLedger("testSchema", "1.0", requiredAttributes)
        val credentialDefinition =
            createCredentialDefinitionAndStoreOnLedger(schema.getSchemaIdObject(), false)
        val credentialOffer =
            createCredentialOffer(credentialDefinition.getCredentialDefinitionIdObject())
        val credentialRequest = createCredentialRequest(walletUser.getIdentityDetails().did, credentialOffer)
        val credential = issueCredentialAndUpdateLedger(credentialRequest, credentialOffer, null) {
            requiredAttributes.forEach {
                attributes[it] = CredentialValue((19 + Random().nextLong()).toString())
            }
        }
        checkLedgerAndReceiveCredential(credential, credentialRequest, credentialOffer)
    }

    fun initFlow(tcName: String, invite: Invite) {
        val indyParty = agentClient.acceptInvite(invite.invite).timeout(30, TimeUnit.SECONDS).toBlocking().value()

        val initRequest = AskForPackageRequest(tcName, invite.clientUUID!!)

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(initRequest, headers)

        val initResponse = this.restTemplate.postForEntity(TreatmentCenterEndpoint.INIT.url, entity, String::class.java)

        if (initResponse.statusCode != HttpStatus.OK) {
            throw RuntimeException("Treatment center is unable to init flow with TC $tcName. Remote server threw error: ${initResponse.body}")
        }

        indyParty.also {
            val proofRequest = it.receiveProofRequest().toBlocking().value()
            val proofInfo = ssiUser.createProofFromLedgerData(proofRequest)
            it.sendProof(proofInfo)

            val credentialOffer = it.receiveCredentialOffer().toBlocking().value()
            val credentialRequest =
                ssiUser.createCredentialRequest(ssiUser.walletUser.getIdentityDetails().did, credentialOffer)
            it.sendCredentialRequest(credentialRequest)

            val credential = it.receiveCredential().toBlocking().value()
            ssiUser.checkLedgerAndReceiveCredential(credential, credentialRequest, credentialOffer)
        }
    }

    fun receivePackage(serial: String) {
        val collectResponse =
            this.restTemplate.postForEntity(TreatmentCenterEndpoint.RECEIVE.url, Serial(serial), String::class.java)

        if (collectResponse.statusCode != HttpStatus.OK) {
            throw RuntimeException("Treatment center is unable to collect package $serial. Remote server threw error: ${collectResponse.body}")
        }
    }

    fun collectPackage(serial: String, invite: Invite) {
        val acceptInvite = agentClient.acceptInvite(invite.invite).timeout(30, TimeUnit.SECONDS).toBlocking().value()

        val collectRequest = Serial(serial, invite.clientUUID)
        val collectResponse =
            this.restTemplate.postForEntity(TreatmentCenterEndpoint.COLLECT.url, collectRequest, String::class.java)

        if (collectResponse.statusCode != HttpStatus.OK) {
            throw RuntimeException("Treatment center is unable to collect package $serial. Remote server threw error: ${collectResponse.body}")
        }

        acceptInvite.also {
            val proofRequest = it.receiveProofRequest().toBlocking().value()
            val proof = ssiUser.createProofFromLedgerData(proofRequest)
            it.sendProof(proof)
        }
    }
}
