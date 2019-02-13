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

package com.luxoft.poc.supplychain.flow.medicine

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.blockchainlab.hyperledger.indy.CredentialDefinitionId
import com.luxoft.blockchainlab.hyperledger.indy.SchemaId
import com.luxoft.poc.supplychain.IndyArtifactsRegistry.ARTIFACT_TYPE
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.flow.*
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap
import java.util.*

// TODO: this flow should be rearranged and renamed to be called from Treatment Center side
class AskNewPackage {

    @CordaSerializable
    data class PackageRequest(val patientDid: String)

    @InitiatingFlow
    @StartableByRPC
    class Patient : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            val flowSession: FlowSession = initiateFlow(getTreatment())

            try {
                val packageRequest = PackageRequest(indyUser().did)
                return flowSession.sendAndReceive<String>(packageRequest).unwrap{ it }

            } catch (e: FlowException) {
                logger.error("Package request can't be processed", e)
                throw FlowException("Request wasnt accepted")
            }
        }
    }

    @InitiatedBy(Patient::class)
    open class Treatment(val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            flowSession.receive<PackageRequest>().unwrap { packageRequest ->
                try {
                    val serial = UUID.randomUUID().toString()

                    requestNewPackage(serial, packageRequest)

                } catch (e: Exception) {
                    logger.error("Patient cant be authenticated", e)
                    throw FlowException(e.message)
                }
            }
        }

        @Suspendable
        private fun requestNewPackage(serial: String, packageRequest: PackageRequest) {
            val patientAgent = flowSession.counterparty.name

            // create new package request
            val packageInfo = PackageInfo(
                    serial = serial,
                    state = PackageState.NEW,
                    patientDid = packageRequest.patientDid,
                    patientAgent = patientAgent,
                    patientDiagnosis = "leukemia",
                    medicineName = "Santorium",
                    medicineDescription = "package-required",
                    requestedBy = ourIdentity.name,
                    processedBy = getManufacturer().name
            )

            subFlow(RequestForPackage.Initiator(packageInfo))

            // create confirmation receipt
            val receiptProposal = IndySchemaBuilder()
                    .addAttr(PackageReceipt.Attributes.Serial, serial)
                    .build()
            val receiptSchemaId = SchemaId(indyUser().did, PackageReceipt.schemaName, PackageReceipt.schemaVersion)
            val receiptCredDef = getCredentialDefinitionBySchemaId(receiptSchemaId)!!

            subFlow(IssueCredentialFlow.Issuer(serial, receiptProposal, receiptCredDef.state.data.credentialDefinitionId, patientAgent))

            flowSession.send(serial)
        }
    }
}
