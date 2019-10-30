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

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyCredentialProof
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.GetInviteFlow
import com.luxoft.poc.supplychain.flow.GetTailsFlow
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.luxoft.poc.supplychain.flow.medicine.GetPackageHistory
import com.luxoft.web.components.RPCComponent
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.Invite
import com.luxoft.web.data.Serial
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*


@RestController
@RequestMapping("api/tc")
@CrossOrigin
@Profile("treatmentcenter")
class TreatmentCenterController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<TreatmentCenterController>()

    @Value("\${indy.trustedCredentialsIssuerDID}")
    lateinit var trustedCredentialsIssuerDID: String

    @PostMapping("package/receive")
    fun receiveShipment(@RequestBody request: Serial) {
        services.startFlowDynamic(ReceiveShipment.Receiver::class.java, AcceptanceResult(request.serial))
    }

    @GetMapping("whoami")

    fun getWhoAmI(): Any {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    @GetMapping("invite")
    fun getInvite(): Invite {
        println("Get request to /api/tc/invite")
        val uuid = UUID.randomUUID()
        val response = services.startFlow(GetInviteFlow::Treatment, uuid).returnValue.getOrThrow(Duration.ofSeconds(15))
        println("Responding to /api/tc/invite with ($uuid,$response)")

        return Invite(response, uuid.toString())
    }

    @GetMapping("tails")
    fun getTails(): Map<String, String> {
        val response = services.startFlow(GetTailsFlow::Treatment).returnValue.getOrThrow(Duration.ofSeconds(15))

        return response
    }

    @PostMapping("request/create")
    fun createPackageRequest(@RequestBody tc: AskForPackageRequest) {
        services.startFlow(AskNewPackage::Treatment, UUID.fromString(tc.clientUUID), trustedCredentialsIssuerDID, tc.serial)
    }

    @PostMapping("package/withdraw")
    fun receivePackage(@RequestBody request: Serial) {
        services.startFlow(PackageWithdrawal::Owner, request.serial, UUID.fromString(request.clientUUID!!))
    }

    @PostMapping("package/history")
    fun packageHistory(@RequestBody request: Serial): Invite {
        val invite = services.startFlow(GetPackageHistory::Requester, request.serial).returnValue.get()
        return Invite(invite)
    }

    @GetMapping("package/list")
    fun getPackageRequests(): Any {
        return try {
            services.vaultQueryBy<Package>().states.map { it.state.data.info }

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @PostMapping("package/proofs")
    fun getPackageProofs(@RequestBody request: Serial): Any {
        return try {
            val r = services.vaultQueryBy<IndyCredentialProof>().states
                .map { it.state.data }
                .filter { it.id == request.serial }
                .map { it.proof.proofData }
            println("Responding to /api/tc/package/proofs with ($r)")
            r
        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }
}
