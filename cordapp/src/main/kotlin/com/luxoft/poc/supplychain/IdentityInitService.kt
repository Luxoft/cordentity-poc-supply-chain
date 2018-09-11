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

package com.luxoft.poc.supplychain

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.AssignPermissionsFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.IssueClaimFlow
import com.luxoft.poc.supplychain.IndyArtifactsRegistry.ARTIFACT_TYPE
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchema
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.flow.ArtifactsManagement
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import java.time.Duration
import java.util.*

class IdentityInitService(private val rpc: CordaRPCOps, private val timeout: Duration = Duration.ofSeconds(30)) {

    fun assignPermissionsToMe(authority: CordaX500Name) {
        val name = rpc.nodeInfo().legalIdentities.first().name.commonName
        rpc.startFlow(AssignPermissionsFlow::Issuer, name, "TRUSTEE", authority).returnValue.getOrThrow(timeout)
    }

    fun initTreatmentIndyMeta() {
        issueIndyMeta(PackageReceipt)
        issueIndyMeta(DiagnosisDetails)
    }

    fun initIssuerIndyMeta() = issueIndyMeta(PersonalInformation)

    fun issueClaimTo(toName: CordaX500Name, credProposal: String, schemaName: String, schemaVer: String) {
        val queryRequest = IndyArtifactsRegistry.QueryRequest(ARTIFACT_TYPE.Definition, schemaName, schemaVer)
        val credDefId = rpc.startFlow(
                ArtifactsManagement::Accessor, queryRequest
        ).returnValue.getOrThrow(timeout)

        val uid = UUID.randomUUID().toString()
        rpc.startFlow(
                IssueClaimFlow::Issuer, uid, credDefId, credProposal, toName
        ).returnValue.getOrThrow(timeout)
    }

    private fun issueIndyMeta(schema: IndySchema) {
        var putRequest: IndyArtifactsRegistry.PutRequest

        // Create package metadata
        val schemaRequest = IndyArtifactsRegistry.IndySchema(schema.schemaName,
                schema.schemaVersion, schema.getSchemaAttrs().map { it.name })

        putRequest = IndyArtifactsRegistry.PutRequest(ARTIFACT_TYPE.Schema,
                SerializationUtils.anyToJSON(schemaRequest))

        val schemaId = rpc.startFlow(
                ArtifactsManagement::Creator, putRequest
        ).returnValue.getOrThrow(timeout)

        val credDefRequest = IndyArtifactsRegistry.IndyCredDef(schemaId)

        putRequest = IndyArtifactsRegistry.PutRequest(ARTIFACT_TYPE.Definition,
                SerializationUtils.anyToJSON(credDefRequest))

        rpc.startFlow(
                ArtifactsManagement::Creator, putRequest
        ).returnValue.getOrThrow(timeout)
    }
}
