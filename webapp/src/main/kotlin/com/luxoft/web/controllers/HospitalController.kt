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

package com.luxoft.web.controllers

import com.luxoft.lumedic.ssi.corda.data.AuthResponse
import com.luxoft.lumedic.ssi.corda.data.AuthState
import com.luxoft.lumedic.ssi.corda.data.state.AuthProcessState
import com.luxoft.lumedic.ssi.corda.flow.AuthPatient
import com.luxoft.lumedic.ssi.corda.flow.DemoReset
import com.luxoft.web.components.RPCComponent
import com.wordnik.swagger.annotations.*
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.time.Duration

@Api(
    value = "Hospital controller",
    produces = MediaType.APPLICATION_JSON_VALUE,
    tags = ["ssi-hospital-controller"],
    description = "Lumedic SSI hospital controller API"
)
@RestController
@RequestMapping("api/hospital")
@CrossOrigin
class HospitalController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<HospitalController>()

    @Value("\${indy.trustedCredentialsIssuerDID}")
    lateinit var trustedCredentialsIssuerDID: String

    @ApiOperation(position = 0, value = "demo/reset", nickname = "DemoReset", notes = "Resets epic and corda data")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Corda transaction id hash which consumed all states",
                response = String::class
            ),
            ApiResponse(code = 500, message = "Backend returned error", response = String::class)
        ]
    )
    @PostMapping("demo/reset")
    fun resetDemo(): String {
        val response = services.startFlow(DemoReset::Hospital).returnValue.getOrThrow(
            //TODO: understand why it takes so much time at first run
            Duration.ofSeconds(120)
        )

        return response.toString()
    }

    @ApiOperation(position = 0, value = "whoami", nickname = "whoami", notes = "Get own node name in X500 format")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Returned own name in X500 format", response = String::class),
            ApiResponse(code = 500, message = "Backend returned error", response = String::class)
        ]
    )
    @GetMapping("whoami")
    fun getWhoAmI(): String {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    @ApiOperation(
        position = 1,
        httpMethod = "POST",
        value = "Starts patient`s treatment authentication",
        nickname = "authPatientTreatment",
        consumes = "application/json"
    )
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Process initiated", response = AuthResponse::class),
            ApiResponse(code = 500, message = "Backend returned error")
        ]
    )
    @PostMapping("auth")
    fun postAuthPatientTreatment(): AuthResponse {
        val response = services.startFlow(AuthPatient::Hospital, trustedCredentialsIssuerDID).returnValue.getOrThrow(
            Duration.ofSeconds(15)
        )

        return response
    }

    @ApiOperation(
        position = 2,
        httpMethod = "GET",
        value = "Returns status of request",
        nickname = "requestState"
    )
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Auth response", response = AuthState::class),
            ApiResponse(code = 500, message = "Backend returned error")
        ]
    )
    @GetMapping("auth/{requestId}")
    fun getRequestState(
        @ApiParam(
            required = true,
            value = "requestId as UUID format string"
        ) @PathVariable requestId: String
    ): AuthState {
        val query =
            QueryCriteria.LinearStateQueryCriteria(externalId = listOf(requestId))
        return services.vaultQueryByCriteria(query, AuthProcessState::class.java).states.map { it.state.data }.single()
            .authState
    }
}
