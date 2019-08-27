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

import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.web.components.flow.TCFlows
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.Invite
import com.luxoft.web.data.Serial
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("api/tc")
@CrossOrigin
@Profile("treatmentcenter")
class TreatmentCenterController(val flowExecutor: TCFlows) {
    private final val logger = loggerFor<TreatmentCenterController>()

    @Value("\${indy.trustedCredentialsIssuerDID:XmLm4WJnNx5poPMqrcgg3q}")
    lateinit var trustedCredentialsIssuerDID: String

    @PostMapping("package/receive")
    fun receiveShipment(@RequestBody request: Serial) {
        flowExecutor.receiveShipment(AcceptanceResult(request.serial))
    }

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return flowExecutor.getNodeName()
    }

    @GetMapping("invite")
    fun getInvite(): Invite {
        println("Get request to /api/tc/invite")
        val uuid = UUID.randomUUID()
        val response = flowExecutor.getInvite(uuid)
        println("Responding to /api/tc/invite with ($uuid,$response)")

        return Invite(response, uuid.toString())
    }

    @PostMapping("request/create")
    fun createPackageRequest(@RequestBody tc: AskForPackageRequest) {
        flowExecutor.askNewPackage(UUID.fromString(tc.clientUUID), trustedCredentialsIssuerDID)
    }

    @PostMapping("package/withdraw")
    fun receivePackage(@RequestBody request: Serial) {
        flowExecutor.packageWithdrawal(request.serial, UUID.fromString(request.clientUUID))
    }

    @PostMapping("package/history")
    fun packageHistory(@RequestBody request: Serial): Invite {
        val invite = flowExecutor.getPackageHistory(request.serial)
        return Invite(invite)
    }

    @GetMapping("package/list")
    fun getPackageRequests(): Any {
        return try {
            flowExecutor.getPackageRequests()
        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

}
