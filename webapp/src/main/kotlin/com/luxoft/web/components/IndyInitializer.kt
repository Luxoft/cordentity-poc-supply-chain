package com.luxoft.web.components

import com.luxoft.poc.supplychain.IdentityInitService
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
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
import net.corda.core.utilities.loggerFor
import net.corda.nodeapi.internal.config.User
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
@Profile("sovrinagent")
class IndyInitializer {
    private final val logger = loggerFor<IndyInitializer>()

    companion object {

        // TODO: How can we rewrite these names to make them look nice like "Marina Bay Hospital" etc???
        val agentCert = CordaX500Name("SovrinAgent", "London", "GB")
        val issuerCert = CordaX500Name("Manufacture", "London", "GB")
        val treatmentCert = CordaX500Name("TreatmentCenter", "London", "GB")

        val diagnosisDetailsProposal = IndySchemaBuilder()
                .addAttr(DiagnosisDetails.Attributes.Stage, "4")
                .addAttr(DiagnosisDetails.Attributes.Disease, "leukemia")
                .addAttr(DiagnosisDetails.Attributes.MedicineName, "package-name")
                .addAttr(DiagnosisDetails.Attributes.Recommendation, "package-required")
                .build()

        val personalInfoProposal = IndySchemaBuilder()
                .addAttr(PersonalInformation.Attributes.Age, "20")
                .addAttr(PersonalInformation.Attributes.Nationality, "eu")
                .addAttr(PersonalInformation.Attributes.Forename, "Mike J")
                .build()
    }

    @PostConstruct
    fun init() {
        val timeout = Duration.ofSeconds(1000L)

        val user = User("user1", "test", permissions = setOf())

        val agent = CordaRPCClient(NetworkHostAndPort("localhost", 10202))
                .start(user.username, user.password).proxy

        val issuer = CordaRPCClient(NetworkHostAndPort("localhost", 10002))
                .start(user.username, user.password).proxy

        val treatment = CordaRPCClient(NetworkHostAndPort("localhost", 10102))
                .start(user.username, user.password).proxy

        logger.info("Created all needed rpc clients")

        val treatmentCenterIdentityService = IdentityInitService(treatment, timeout)
        treatmentCenterIdentityService.initTreatmentIndyMeta()
        treatmentCenterIdentityService.issueClaimTo(agentCert,
                diagnosisDetailsProposal,
                DiagnosisDetails.schemaName,
                DiagnosisDetails.schemaVersion)

        logger.info("Treatment center indy stuff initialized")

        val issuerIdentityService = IdentityInitService(issuer, timeout)
        issuerIdentityService.assignPermissionsToMe(treatmentCert)
        issuerIdentityService.initIssuerIndyMeta()
        issuerIdentityService.issueClaimTo(agentCert,
                personalInfoProposal,
                PersonalInformation.schemaName,
                PersonalInformation.schemaVersion)

        logger.info("Successfully initialized all indy stuff")

        val chainOfAuthority = ChainOfAuthority()
                .add(BusinessEntity.Treatment, treatmentCert)
                .add(BusinessEntity.Manufacturer, issuerCert)
                .add(BusinessEntity.Insuranse, treatmentCert)
                .add(BusinessEntity.Goverment, issuerCert)

        logger.info("Creating test package...")

        val askNewPackageRes = agent.startFlowDynamic(AskNewPackage.Patient::class.java, chainOfAuthority)
        val serial = askNewPackageRes.returnValue.getOrThrow(timeout)

        logger.info("1. Package request created")

        val deliverShipmentRes = issuer.startFlowDynamic(DeliverShipment.Sender::class.java, serial, treatmentCert)
        deliverShipmentRes.returnValue.getOrThrow(timeout)

        logger.info("2. Package request processed, package delivered pack")

        val acceptanceRes = AcceptanceResult(serial)
        val receiveShipmentRes = treatment.startFlowDynamic(ReceiveShipment.Receiver::class.java, acceptanceRes)
        receiveShipmentRes.returnValue.getOrThrow(timeout)

        logger.info("3. Package shipped")

        val packageWithdrawalRes = agent.startFlowDynamic(PackageWithdrawal.Owner::class.java, serial)
        packageWithdrawalRes.returnValue.getOrThrow(timeout)

        logger.info("4. Package withdrawal success")

        val packages = agent.vaultQueryBy<Package>().states.map { it.state.data.info }
        logger.info("Packages in vault: $packages")
        logger.info("Initialization passed")
    }
}