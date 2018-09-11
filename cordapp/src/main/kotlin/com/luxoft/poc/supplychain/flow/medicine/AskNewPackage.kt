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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.GetDidFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.IssueClaimFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.VerifyClaimFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
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
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap
import java.util.*

class AskNewPackage {

    @CordaSerializable
    data class PackageRequest(val patientDid: String,
                              val authorities: ChainOfAuthority)

    @InitiatingFlow
    @StartableByRPC
    class Patient(private val authorities: ChainOfAuthority) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            require(authorities.chain.containsKey(BusinessEntity.Treatment)) { "Treatment has to be specified" }

            val treatment = whoIs(authorities.chain[BusinessEntity.Treatment]!!)
            val flowSession: FlowSession = initiateFlow(treatment)

            try {
                val packageRequest = PackageRequest(indyUser().did, authorities)
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

                    require(authenticateUser(serial, packageRequest.authorities)) {
                        "Patient cant request new package. Minimal requirements not matched"
                    }

                    requestNewPackage(serial, packageRequest)

                } catch (e: Exception) {
                    logger.error("Patient cant be authenticated", e)
                    throw FlowException(e.message)
                }
            }
        }

        @Suspendable
        private fun authenticateUser(serial: String, authorities: ChainOfAuthority): Boolean {
            require(authorities.chain.containsKey(BusinessEntity.Goverment)) { "Government has to be specified" }
            require(authorities.chain.containsKey(BusinessEntity.Insuranse)) { "Insurance has to be specified" }

            val insurance = authorities.chain[BusinessEntity.Insuranse]!!
            val goverment = authorities.chain[BusinessEntity.Goverment]!!

            val govermentDid = subFlow(GetDidFlow.Initiator(goverment))
            val insuranceDid = subFlow(GetDidFlow.Initiator(insurance))

            val diagnosisSchemaId = getArtifactId(ARTIFACT_TYPE.Schema, DiagnosisDetails, insurance)
            val diagnosisCredDefId = getArtifactId(ARTIFACT_TYPE.Definition, DiagnosisDetails, insurance)

            val personalitySchemaId = getArtifactId(ARTIFACT_TYPE.Schema, PersonalInformation, goverment)
            val personalityCredDefId = getArtifactId(ARTIFACT_TYPE.Definition, PersonalInformation, goverment)

            val attributes = listOf(
                    VerifyClaimFlow.ProofAttribute(diagnosisSchemaId, diagnosisCredDefId, insuranceDid, DiagnosisDetails.Attributes.Disease.name),
                    VerifyClaimFlow.ProofAttribute(diagnosisSchemaId, diagnosisCredDefId, insuranceDid, DiagnosisDetails.Attributes.MedicineName.name),
                    VerifyClaimFlow.ProofAttribute(diagnosisSchemaId, diagnosisCredDefId, insuranceDid, DiagnosisDetails.Attributes.Recommendation.name),

                    VerifyClaimFlow.ProofAttribute(personalitySchemaId, personalityCredDefId, govermentDid, PersonalInformation.Attributes.Nationality.name, "eu")
            )

            // TODO: get Constants from Config!!!
            val predicates = listOf(
                    VerifyClaimFlow.ProofPredicate(diagnosisSchemaId, diagnosisCredDefId, insuranceDid, DiagnosisDetails.Attributes.Stage.name, 3),
                    VerifyClaimFlow.ProofPredicate(personalitySchemaId, personalityCredDefId, govermentDid, PersonalInformation.Attributes.Age.name, 18)
            )

            val proverName = flowSession.counterparty.name
            return subFlow(VerifyClaimFlow.Verifier(serial, attributes, predicates, proverName))
        }

        @Suspendable
        private fun requestNewPackage(serial: String, packageRequest: PackageRequest) {
            require(packageRequest.authorities.chain.containsKey(BusinessEntity.Manufacturer)) { "Manufacturer has to be specified" }

            val proof = getClaimProof(serial).state.data.proof
            val proofReq = getClaimProof(serial).state.data.proofReq.json.toString()

            val patientAgent = flowSession.counterparty.name

            // create new package request
            val packageInfo = PackageInfo(
                    serial = serial,
                    state = PackageState.NEW,
                    patientDid = packageRequest.patientDid,
                    patientAgent = patientAgent,
                    patientDiagnosis = proof.getAttributeValue(DiagnosisDetails.Attributes.Disease.name, proofReq),
                    medicineName = proof.getAttributeValue(DiagnosisDetails.Attributes.MedicineName.name, proofReq),
                    medicineDescription = proof.getAttributeValue(DiagnosisDetails.Attributes.Recommendation.name, proofReq),
                    requestedBy = ourIdentity.name,
                    processedBy = packageRequest.authorities.chain[BusinessEntity.Manufacturer]!!)

            subFlow(RequestForPackage.Initiator(packageInfo, packageRequest.authorities.chain[BusinessEntity.Manufacturer]!!))

            // create confirmation receipt
            val receiptProposal = IndySchemaBuilder()
                    .addAttr(PackageReceipt.Attributes.Serial, serial)
                    .build()
            val receiptCredDefId = getCacheCredDefId(PackageReceipt)

            subFlow(IssueClaimFlow.Issuer(serial, receiptCredDefId, receiptProposal, patientAgent))

            flowSession.send(serial)
        }
    }
}
