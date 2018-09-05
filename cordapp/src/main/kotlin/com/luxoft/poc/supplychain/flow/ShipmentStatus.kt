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

package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.supplychain.data.ProcessResult
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.state.getObservers
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap


object ShipmentStatus {

    @InitiatingFlow
    @StartableByRPC
    class Notifier(private val acceptanceCheck: AcceptanceResult) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val packageRef = getPackageState(acceptanceCheck.serial, ourIdentity)

            val flowToObservers = packageRef.getObservers()
                    .except(ourIdentity)
                    .runSessions(this)

            if(acceptanceCheck.isAccepted) {
                flowToObservers.forEach { it.send(ProcessResult.Success()) }
            } else flowToObservers.forEach {
                val shipmentRef = getShipmentState(acceptanceCheck.serial, true)
                it.send(ProcessResult.Failure(shipmentRef.ref, acceptanceCheck))
            }
        }
    }

    @InitiatedBy(Notifier::class)
    class Observer(private val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        fun handleFailure(shipment: StateRef, qualityCheck: AcceptanceResult) {
            // TODO: package wasnt accepted - need to handle it
        }

        @Suspendable
        override fun call() {
            flowSession.receive<ProcessResult>().unwrap { processResult ->
                when (processResult) {
                    is ProcessResult.Failure -> handleFailure(processResult.state, processResult.qualityCheck)
                    is ProcessResult.Success -> Unit
                }
            }
        }
    }
}
