package com.luxoft.web.components.flow

import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.poc.supplychain.flow.GetInviteFlow
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import com.luxoft.poc.supplychain.flow.medicine.GetPackageHistory
import com.luxoft.web.components.RPCComponent
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
@Profile("manufacture & corda")
class MFFlowsCorda(rpc: RPCComponent) : MFFlows {
    private final val services = rpc.services

    override fun getNodeName(): String {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    override fun getPackageRequests(): List<PackageInfo> {
        return services.vaultQueryBy<Package>().states.map { it.state.data.info }
    }

    override fun getPackageHistory(serial: String): String {
        return services.startFlow(GetPackageHistory::Requester, serial).returnValue.get()
    }

    override fun deliverShipment(serial: String, name: CordaX500Name) {
        services.startFlowDynamic(DeliverShipment.Sender::class.java, serial, name).returnValue.get()
    }
}

@Service
@Profile("treatmentcenter & corda")
class TCFlowsCorda(rpc: RPCComponent) : TCFlows {
    private final val services = rpc.services

    override fun getNodeName(): String {
        return services.nodeInfo().legalIdentities.first().name.organisation
    }

    override fun getPackageRequests(): List<PackageInfo> {
        return services.vaultQueryBy<Package>().states.map { it.state.data.info }
    }

    override fun getPackageHistory(serial: String): String {
        return services.startFlow(GetPackageHistory::Requester, serial).returnValue.get()
    }

    override fun receiveShipment(result: AcceptanceResult) {
        services.startFlowDynamic(ReceiveShipment.Receiver::class.java, result)
    }

    override fun getInvite(uuid: UUID): String {
        return services.startFlow(GetInviteFlow::Treatment, uuid).returnValue.getOrThrow(Duration.ofSeconds(15))
    }

    override fun askNewPackage(uuid: UUID, did: String) {
        services.startFlow(AskNewPackage::Treatment, uuid, did)
    }

    override fun packageWithdrawal(serial: String, clientId: UUID) {
        services.startFlow(PackageWithdrawal::Owner, serial, clientId)
    }
}
