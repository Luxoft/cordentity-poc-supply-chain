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

package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.poc.supplychain.IndyArtifactsRegistry
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchema
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.flow.ArtifactsManagement
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.node.internal.StartedNode
import net.corda.testing.node.internal.InternalMockNetwork
import net.corda.testing.node.internal.startFlow
import org.junit.After
import org.junit.Before

import java.time.Duration
import java.util.*

abstract class IdentityBase(val config: NetworkConfiguration) {

    companion object {
        val logger = loggerFor<IdentityBase>()
    }


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

        logger.info("creating new Indy Meta for: ${schema}, Issuer ${issuer.getName().organisation}")

        var putRequest: IndyArtifactsRegistry.PutRequest

        // create schema
        val schemaRequest = IndyArtifactsRegistry.IndySchema(schema.schemaName,
                schema.schemaVersion, schema.getSchemaAttrs().map { it.name })

        putRequest = IndyArtifactsRegistry.PutRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Schema,
                SerializationUtils.anyToJSON(schemaRequest))

        val schemaResFuture = issuer.services.startFlow(
                ArtifactsManagement.Creator(putRequest)).resultFuture

        logger.info("Request for new schema is submitted: ${putRequest}")

        config.runNetwork()
        val schemaId = schemaResFuture.getOrThrow(Duration.ofSeconds(30))

        // create definition
        val credDefRequest = IndyArtifactsRegistry.IndyCredDef(schemaId)
        putRequest = IndyArtifactsRegistry.PutRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Definition,
                SerializationUtils.anyToJSON(credDefRequest))

        val credDefResFuture = issuer.services.startFlow(
                ArtifactsManagement.Creator(putRequest)).resultFuture

        logger.info("Request for new definition is submitted: ${putRequest}")

        config.runNetwork()
        credDefResFuture.getOrThrow(Duration.ofSeconds(30))
    }

    fun issueClaim(credDesc: CredentialDesc,
                   to: StartedNode<InternalMockNetwork.MockNode>) {

        logger.info("creating new credentials " +
                "${credDesc.issuer.getName().organisation} -> ${to.getName().organisation}:" +
                "${credDesc.credDefId}:${credDesc.credProposal}: ")

        val uid = UUID.randomUUID().toString()

        val future = credDesc.issuer.services.startFlow(
                IssueClaimFlow.Issuer(uid,
                        credDesc.credDefId,
                        credDesc.credProposal,
                        to.getName())).resultFuture

        logger.info("Request for new credentials is submitted: ${uid}")

        config.runNetwork()
        future.getOrThrow(Duration.ofSeconds(30))
    }

    fun getCredDefId(owner: StartedNode<InternalMockNetwork.MockNode>,
                     schema: IndySchema): String {
        logger.info("getting existing credentials definition id from the registry " +
                "${owner.getName().organisation}: {$schema}")

        val queryRequest = IndyArtifactsRegistry.QueryRequest(IndyArtifactsRegistry.ARTIFACT_TYPE.Definition,
                schema.schemaName, schema.schemaVersion)

        val schemaResFuture = owner.services.startFlow(
                ArtifactsManagement.Accessor(queryRequest)).resultFuture

        logger.info("Request for credential definition id is submitted")

        config.runNetwork()
        return schemaResFuture.getOrThrow(Duration.ofSeconds(30))
    }
}
