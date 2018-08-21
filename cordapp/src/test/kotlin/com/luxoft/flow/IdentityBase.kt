package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.poc.supplychain.IndyArtifactsRegistry
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchema
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.flow.ArtifactsManagement
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.startFlow
import org.junit.After
import org.junit.Before

import java.time.Duration
import java.util.*

abstract class IdentityBase(val config: NetworkConfiguration) {

    data class CredentialDesc(val credProposal: String,
                              val credDefId: String,
                              val issuer: StartedNode<InternalMockNetwork.MockNode>)

    @Before
    fun setup() {
        config.up()

        createIndyMeta(config.treatment, PackageReceipt)
        createIndyMeta(config.insurance, DiagnosisDetails)
        createIndyMeta(config.goverment, PersonalInformation)

        onUp()
    }

    @After
    fun down() {
        config.down()
        onDown()
    }

    abstract fun onUp()
    abstract fun onDown()

    private fun createIndyMeta(issuer: StartedNode<InternalMockNetwork.MockNode>,
                               schema: IndySchema) {

        var putRequest: IndyArtifactsRegistry.PutRequest

        // create schema
        val schemaRequest = IndyArtifactsRegistry.IndySchema(schema.schemaName,
                schema.schemaVersion, schema.getSchemaAttrs().map { it.name })

        putRequest = IndyArtifactsRegistry.PutRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Schema,
                SerializationUtils.anyToJSON(schemaRequest))

        val schemaResFuture = issuer.services.startFlow(
                ArtifactsManagement.Creator(putRequest)).resultFuture

        config.runNetwork()
        val schemaId = schemaResFuture.getOrThrow(Duration.ofSeconds(30))

        // create definition
        val credDefRequest = IndyArtifactsRegistry.IndyCredDef(schemaId)
        putRequest = IndyArtifactsRegistry.PutRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Definition,
                SerializationUtils.anyToJSON(credDefRequest))

        val credDefResFuture = issuer.services.startFlow(
                ArtifactsManagement.Creator(putRequest)).resultFuture

        config.runNetwork()
        credDefResFuture.getOrThrow(Duration.ofSeconds(30))
    }

    fun issueClaim(credDesc: CredentialDesc,
                   to: StartedNode<InternalMockNetwork.MockNode>) {
        val uid = UUID.randomUUID().toString()

        val future = credDesc.issuer.services.startFlow(
                IssueClaimFlow.Issuer(uid,
                        credDesc.credDefId,
                        credDesc.credProposal,
                        to.getName())).resultFuture

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))
    }

    fun getCredDefId(owner: StartedNode<InternalMockNetwork.MockNode>,
                     schema: IndySchema): String {
        val queryRequest = IndyArtifactsRegistry.QueryRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Definition,
                schema.schemaName, schema.schemaVersion)

        val schemaResFuture = owner.services.startFlow(
                ArtifactsManagement.Accessor(queryRequest)).resultFuture
        config.runNetwork()

        return schemaResFuture.getOrThrow(Duration.ofSeconds(30))
    }
}