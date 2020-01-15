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
import com.luxoft.lumedic.ssi.corda.data.AuthResponse
import com.luxoft.lumedic.ssi.corda.data.AuthState
import com.luxoft.web.e2e.getValue
import com.luxoft.web.e2e.waitThenAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

enum class TreatmentCenterEndpoint(val url: String) {
    AUTH("/api/hospital/auth"),
}

@Component
class HospitalClient {
    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    lateinit var ssiUser: SsiUser

    @Autowired
    lateinit var agentClient: AgentConnection

    @Value("\${hospitalEndpoint}")
    lateinit var endpoint: String

    private lateinit var restTemplate: RestTemplate

    val syncUpRetry = 30

    @PostConstruct
    fun init() {
        restTemplate = restTemplateBuilder.rootUri(endpoint).build()
    }

    fun getRequestState(requestId: String): AuthState =
        this.restTemplate.getForObject("${TreatmentCenterEndpoint.AUTH.url}/$requestId")
            ?: throw RuntimeException("Failed to request packages")

    fun postAuthRequest(): AuthResponse =
        restTemplate.postForObject(TreatmentCenterEndpoint.AUTH.url) ?: throw RuntimeException("Failed to get invite")

    fun authPatientFlow() {
        val authResponse = postAuthRequest()
        println("RequestId:${authResponse.requestId}")

        var authProcessState = getRequestState(authResponse.requestId)
        assert(authProcessState == AuthState.INVITED)

        val indyParty = agentClient.acceptInvite(authResponse.invite).timeout(30, TimeUnit.SECONDS).getValue()

        waitThenAssert(syncUpRetry) {
            authProcessState = getRequestState(authResponse.requestId)

            authProcessState == AuthState.CONNECTED
        }

        indyParty.also {
            val proofRequest = it.receiveProofRequest().getValue()
            val proofInfo = ssiUser.createProofFromLedgerData(proofRequest)
            it.sendProof(proofInfo)
        }
        waitThenAssert(syncUpRetry) {
            authProcessState = getRequestState(authResponse.requestId)

            authProcessState == AuthState.SUCCESS
        }
    }
}
