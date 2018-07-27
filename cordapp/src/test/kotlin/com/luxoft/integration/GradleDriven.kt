package com.luxoft.integration

import com.luxoft.poc.supplychain.IdentityService
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.flow.DeliverShipment
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import com.luxoft.poc.supplychain.flow.ReceiveShipment
import com.luxoft.poc.supplychain.flow.medicine.AskNewPackage
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.User
import java.time.Duration


class GradleDriven: e2eBase {

    val agentCert = CordaX500Name("SovrinAgent", "London", "GB")
    val issuerCert  = CordaX500Name("Manufacture", "London", "GB")
    val treatmentCert = CordaX500Name("TreatmentCenter", "London", "GB")
    val artifactoryCert = CordaX500Name("Artifactory", "London", "GB")

    override fun execute() {

        val user = User("user1", "test", permissions = setOf())

        val host = "localhost"

        val agent = CordaRPCClient(NetworkHostAndPort(host, 10202))
                .start(user.username, user.password).proxy

        val issuer = CordaRPCClient(NetworkHostAndPort(host, 10002))
                .start(user.username, user.password).proxy

        val treatment = CordaRPCClient(NetworkHostAndPort(host, 10102))
                .start(user.username, user.password).proxy

        val treatmentCenterIdentityService = IdentityService(treatment)
        treatmentCenterIdentityService.initTreatmentIndy()

        val manufactureIdentityService = IdentityService(issuer)
        manufactureIdentityService.initIssuerIndy()

        val chainOfAuthority = ChainOfAuthority()
                .add(BusinessEntity.Treatment, treatmentCert)
                .add(BusinessEntity.Manufacturer, issuerCert)
                .add(BusinessEntity.Insuranse, treatmentCert)
                .add(BusinessEntity.Goverment, issuerCert)
                .add(BusinessEntity.Artifactory, artifactoryCert)

        println("Indy step finished")

        val askNewPackageRes = agent.startFlowDynamic(AskNewPackage.Patient::class.java, chainOfAuthority)
        val serial = askNewPackageRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 1 finished")

        val deliverShipmentRes = issuer.startFlowDynamic(DeliverShipment.Sender::class.java, serial, treatmentCert)
        deliverShipmentRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 2 finished")

        val acceptanceRes = AcceptanceResult(serial)
        val receiveShipmentRes = treatment.startFlowDynamic(ReceiveShipment.Receiver::class.java, acceptanceRes)
        receiveShipmentRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 3 finished")

        val packageWithdrawalRes = agent.startFlowDynamic(PackageWithdrawal.Owner::class.java, serial)
        packageWithdrawalRes.returnValue.getOrThrow(Duration.ofSeconds(30))

        println("Step 4 finished")

        val packages = agent.vaultQueryBy<Package>().states.map { it.state.data.info }
        println("Packages: $packages")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            GradleDriven().execute()
        }
    }
}