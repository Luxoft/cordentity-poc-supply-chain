package com.luxoft.web.components.flow

import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageInfo
import net.corda.core.identity.CordaX500Name
import java.util.*

interface CommonFlows {
    fun getNodeName(): String
    fun getPackageRequests(): List<PackageInfo>
    fun getPackageHistory(serial: String): String
}

interface TCFlows : CommonFlows {
    fun receiveShipment(result: AcceptanceResult)
    fun getInvite(uuid: UUID): String
    fun askNewPackage(uuid: UUID, issuerDid: String)
    fun packageWithdrawal(serial: String, clientId: UUID)
}

interface MFFlows : CommonFlows {
    fun deliverShipment(serial: String, name: CordaX500Name)
}
