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

import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.web.components.RPCComponent
import com.luxoft.web.data.FAILURE
import com.luxoft.web.data.Serial
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.loggerFor
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/mf")
@CrossOrigin
@Profile("manufacture")
class ManufactureController(rpc: RPCComponent) {
    private final val services = rpc.services
    private final val logger = loggerFor<ManufactureController>()

    @GetMapping("whoami")
    fun getWhoAmI(): Any {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    @GetMapping("package/list")
    fun getPackageRequests(): Any {

        return try {
            val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            val a = services.vaultQueryBy<Package>().states.map { it.state.data.info }

            logger.info(a.size.toString())

            a
        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }

    @PostMapping("request/process")
    fun processPackageRequest(@RequestBody serial: Serial): Any? {

        return try {

            val flowHandle = services.startFlowDynamic(DeliverShipment.Sender::class.java, serial.serial, CordaX500Name("TreatmentCenter", "London", "GB"))
            flowHandle.returnValue.get()
            null

        } catch (e: Exception) {
            logger.error("", e)
            FAILURE.plus("error" to e.message)
        }
    }
}
