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

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.GetInviteFlow
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.luxoft.web.components.RPCComponent
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.Serial
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/tc")
@CrossOrigin
@Profile("treatmentcenter")
class TreatmentCenterController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<TreatmentCenterController>()

    @PostMapping("package/receive")
    fun receiveShipment(@RequestBody request: Serial): Any? {

        return try {

            val flowHandle = services.startFlowDynamic(ReceiveShipment.Receiver::class.java, AcceptanceResult(request.serial))
            flowHandle.returnValue.get()
            null

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    @GetMapping("invite")
    fun getInvite(): AgentConnection.ReceiveInviteMessage {
        return services.startFlow(GetInviteFlow::Treatment).returnValue.get()
    }

    @PostMapping("request/create")
    fun createPackageRequest(@RequestBody tc: AskForPackageRequest) {
        services.startFlow(AskNewPackage::Treatment)
    }

    @PostMapping("package/withdraw")
    fun receivePackage(@RequestBody request: Serial) {
        services.startFlow(PackageWithdrawal::Owner, request.serial)
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

}
