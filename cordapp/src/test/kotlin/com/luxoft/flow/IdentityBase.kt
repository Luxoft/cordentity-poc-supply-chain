package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.startFlow
import org.junit.After
import org.junit.Before

import java.time.Duration
import java.util.*

abstract class IdentityBase(val config: NetworkConfiguration) {

    data class ClaimDescriptor(val proposal: String,
                               val name: String,
                               val version: String,
                               val issuer: StartedNode<InternalMockNetwork.MockNode>) {
        fun getSchema() = IndyUser.SchemaDetails(name, version,  issuer.getPartyDid())
    }

    @Before
    fun setup() {
        config.up()

        createDiagnosisMeta()
        createPackageMeta()
        createPersonalMeta()

        onUp()
    }

    @After
    fun down() {
        config.down()
        onDown()
    }

    abstract fun onUp()
    abstract fun onDown()

    private fun createPackageMeta() {
        val schemaResFuture = config.treatment.services.startFlow(
                CreateSchemaFlow.Authority(
                        PackageReceipt.schemaName,
                        PackageReceipt.schemaVersion,
                        PackageReceipt().getSchemaAttrs().map { it.name })).resultFuture

        config.runNetwork()
        schemaResFuture.getOrThrow(Duration.ofSeconds(30))

        val future = config.treatment.services.startFlow(
                CreateClaimDefFlow.Authority(
                        config.treatment.getPartyDid(),
                        PackageReceipt.schemaName,
                        PackageReceipt.schemaVersion)).resultFuture

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))
    }

    private fun createDiagnosisMeta() {
        val schemaResFuture = config.insurance.services.startFlow(
                CreateSchemaFlow.Authority(
                        DiagnosisDetails.schemaName,
                        DiagnosisDetails.schemaVersion,
                        DiagnosisDetails().getSchemaAttrs().map { it.name })).resultFuture

        config.runNetwork()
        schemaResFuture.getOrThrow(Duration.ofSeconds(30))

        val future = config.insurance.services.startFlow(
                CreateClaimDefFlow.Authority(
                        config.insurance.getPartyDid(),
                        DiagnosisDetails.schemaName,
                        DiagnosisDetails.schemaVersion)).resultFuture

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))
    }

    private fun createPersonalMeta() {
        val schemaResFuture = config.goverment.services.startFlow(
                CreateSchemaFlow.Authority(
                        PersonalInformation.schemaName,
                        PersonalInformation.schemaVersion,
                        PersonalInformation().getSchemaAttrs().map { it.name })).resultFuture

        config.runNetwork()
        schemaResFuture.getOrThrow(Duration.ofSeconds(30))

        val future = config.goverment.services.startFlow(
                CreateClaimDefFlow.Authority(
                        config.goverment.getPartyDid(),
                        PersonalInformation.schemaName,
                        PersonalInformation.schemaVersion)).resultFuture

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))

    }

    fun issueClaim(claimDesc: ClaimDescriptor,
                   to: StartedNode<InternalMockNetwork.MockNode>) {
        val uid = UUID.randomUUID().toString()

        val future = claimDesc.issuer.services.startFlow(
                IssueClaimFlow.Issuer(uid, claimDesc.getSchema(), claimDesc.proposal, to.getName())
        ).resultFuture

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))
    }
}