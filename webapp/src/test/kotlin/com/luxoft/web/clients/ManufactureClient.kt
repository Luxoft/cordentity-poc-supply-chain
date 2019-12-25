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
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.models.Interval
import com.luxoft.blockchainlab.hyperledger.indy.utils.*
import com.luxoft.poc.supplychain.data.AuthorityInfoMap
import com.luxoft.poc.supplychain.data.schema.PackageIndySchema
import com.luxoft.web.data.Invite
import com.luxoft.web.data.PackagesResponse
import com.luxoft.web.data.ProcessPackageRequest
import com.luxoft.web.data.Serial
import com.luxoft.web.e2e.getValue
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
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

enum class ManufactureEndpoint(val url: String) {
    LIST("/api/mf/package/list"),
    PROCESS("/api/mf/request/process"),
    HISTORY("/api/mf/package/history"),
}

@Component
class ManufactureClient {
    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Value("\${manufactureEndpoint}")
    lateinit var endpoint: String

    @Autowired
    lateinit var ssiUser: SsiUser

    @Autowired
    lateinit var agentClient: AgentConnection

    private lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        restTemplate = restTemplateBuilder.rootUri(endpoint).build()
    }

    fun getPackages(): PackagesResponse = this.restTemplate.getForObject(ManufactureEndpoint.LIST.url)
        ?: throw RuntimeException("Failed to request packages")

    fun processPackage(serial: String) {
        val processRequest = ProcessPackageRequest(serial)

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(processRequest, headers)

        val processResponse = this.restTemplate.postForEntity(ManufactureEndpoint.PROCESS.url, entity, String::class.java)

        if (processResponse.statusCode != HttpStatus.OK) {
            throw RuntimeException("Server threw error: ${processResponse.body}")
        }
    }

    fun packageHistory(serial: String) {
        val request = Serial(serial)
        val response =
            restTemplate.postForEntity(ManufactureEndpoint.HISTORY.url, request, Invite::class.java)

        if (response.statusCode != HttpStatus.OK) {
            throw RuntimeException("Treatment center is unable to get history invite for package $serial. Remote server threw error: ${response.body}")
        }

        val acceptInvite =
            agentClient.acceptInvite(response.body.invite).timeout(30, TimeUnit.SECONDS).getValue()

        val packageCredential = ssiUser.walletUser.getCredentials().asSequence().find {
            it.getSchemaIdObject().name.contains(PackageIndySchema.schemaName.split("-").first()) &&
                    it.attributes["serial"] == serial
        }!!
        val authorities =
            SerializationUtils.jSONToAny<AuthorityInfoMap>(packageCredential.attributes["authorities"].toString())

        assert(authorities.isNotEmpty())

        acceptInvite.also {
            run {
                val proofRequest = it.receiveProofRequest().getValue()
                val proofInfo = ssiUser.createProofFromLedgerData(proofRequest)
                it.sendProof(proofInfo)
            }

            val provedAuthorities = authorities.mapValues { (_, authority) ->
                val proofRequest = proofRequest("package_history_req", "1.0") {
                    reveal("status") {
                        "serial" shouldBe serial
                        FilterProperty.IssuerDid shouldBe authority.did
                        FilterProperty.SchemaId shouldBe authority.schemaId
                    }
                    reveal("time") {
                        "serial" shouldBe serial
                        FilterProperty.IssuerDid shouldBe authority.did
                        FilterProperty.SchemaId shouldBe authority.schemaId
                    }
                    proveNonRevocation(Interval.allTime())
                }
                it.sendProofRequest(proofRequest)
                val proof = it.receiveProof().getValue()
                assert(ssiUser.verifyProofWithLedgerData(proofRequest, proof))
                proof
            }
            println(provedAuthorities)
        }
    }
}
