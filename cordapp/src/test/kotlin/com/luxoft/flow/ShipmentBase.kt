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
                serial = UUID.randomUUID().toString(),
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

    protected fun runShipment(serial: String,
                              from: StartedNode<InternalMockNetwork.MockNode>,
                              to: StartedNode<InternalMockNetwork.MockNode>) {

        val flowStartShipment = DeliverShipment.Sender(serial, to.getName())
        val future = from.services.startFlow(flowStartShipment).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }

    protected fun endShipment(serial: String, destination: StartedNode<InternalMockNetwork.MockNode>) {

        val fLowEndShipment = ReceiveShipment.Receiver(AcceptanceResult(serial))
        val future = destination.services.startFlow(fLowEndShipment).resultFuture
        config.runNetwork()
        future.getOrThrow()
    }
}