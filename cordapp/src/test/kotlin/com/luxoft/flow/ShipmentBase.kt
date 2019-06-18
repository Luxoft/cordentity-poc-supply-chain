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
 *//*


package com.luxoft.flow

import com.luxoft.poc.supplychain.data.*
import com.luxoft.poc.supplychain.flow.*
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.startFlow
import java.util.*

open class ShipmentBase(config: NetworkConfiguration): IdentityBase(config)  {

    lateinit var packageInfo: PackageInfo

    override fun onDown() = Unit

    override fun onUp() {
        packageInfo = PackageInfo(
                packageId = UUID.randomUUID().toString(),
                state = PackageState.NEW,
                patientDid = config.agent.getPartyDid(),
                patientAgent = config.agent.getName(),
                patientDiagnosis = "leukemia",
                medicineName = "",
                medicineDescription = "",
                requestedBy = config.treatment.getName())
    }

    protected fun askForPackage(initiator: StartedNode<InternalMockNetwork.MockNode>, chainOfAuthority: ChainOfAuthority) {

        val flowAskForPackage = AskNewPackage.Patient(chainOfAuthority)
        val future = initiator.services.startFlow(flowAskForPackage).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }

    protected fun newPackageRequest(from: StartedNode<InternalMockNetwork.MockNode>,
                                    to: StartedNode<InternalMockNetwork.MockNode>,
                                    packageInfo: PackageInfo) {

        val flowPackageRequest = RequestForPackage.Initiator(packageInfo, to.getName())
        val future = from.services.startFlow(flowPackageRequest).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }

    protected fun runShipment(packageId: String,
                              from: StartedNode<InternalMockNetwork.MockNode>,
                              to: StartedNode<InternalMockNetwork.MockNode>) {

        val flowStartShipment = DeliverShipment.Sender(packageId, to.getName())
        val future = from.services.startFlow(flowStartShipment).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }

    protected fun endShipment(packageId: String, destination: StartedNode<InternalMockNetwork.MockNode>) {

        val fLowEndShipment = ReceiveShipment.Receiver(AcceptanceResult(packageId))
        val future = destination.services.startFlow(fLowEndShipment).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }
}
*/
