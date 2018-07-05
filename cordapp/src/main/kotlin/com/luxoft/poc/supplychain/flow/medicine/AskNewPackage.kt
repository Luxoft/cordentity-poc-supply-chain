package com.luxoft.poc.supplychain.flow.medicine

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.GetDidFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.IssueClaimFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.VerifyClaimFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.poc.supplychain.data.BusinessEntity
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.flow.RequestForPackage
import com.luxoft.poc.supplychain.flow.getClaimProof
import com.luxoft.poc.supplychain.flow.whoIs
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
                logger.error("", e)
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
                    logger.error("", e)
                    throw FlowException(e.message)
                }
            }
        }

        @Suspendable
        private fun authenticateUser(serial: String, authorities: ChainOfAuthority): Boolean {
            require(authorities.chain.containsKey(BusinessEntity.Goverment)) { "Government has to be specified" }
            require(authorities.chain.containsKey(BusinessEntity.Insuranse)) { "Insurance has to be specified" }

            val govermentDid = subFlow(GetDidFlow.Initiator(authorities.chain[BusinessEntity.Goverment]!!))
            val insuranceDid = subFlow(GetDidFlow.Initiator(authorities.chain[BusinessEntity.Insuranse]!!))

            val diagnosisSchema = IndyUser.SchemaDetails(
                    DiagnosisDetails.schemaName,
                    DiagnosisDetails.schemaVersion,
                    insuranceDid)

            val personalitySchema = IndyUser.SchemaDetails(
                    PersonalInformation.schemaName,
                    PersonalInformation.schemaVersion,
                    govermentDid)

            val attributes = listOf(
                    IndyUser.ProofAttribute(diagnosisSchema, DiagnosisDetails.Attributes.Disease.name),
                    IndyUser.ProofAttribute(diagnosisSchema, DiagnosisDetails.Attributes.MedicineName.name),
                    IndyUser.ProofAttribute(diagnosisSchema, DiagnosisDetails.Attributes.Recommendation.name),

                    IndyUser.ProofAttribute(personalitySchema, PersonalInformation.Attributes.Nationality.name, "eu")
            )

            // TODO: get Constants from Config!!!
            val predicates = listOf(
                    IndyUser.ProofPredicate(diagnosisSchema, DiagnosisDetails.Attributes.Stage.name, 3),
                    IndyUser.ProofPredicate(personalitySchema, PersonalInformation.Attributes.Age.name, 18)
            )

            val prover = flowSession.counterparty.name
            return subFlow(VerifyClaimFlow.Verifier(serial, attributes, predicates, prover))
        }

        @Suspendable
        private fun requestNewPackage(serial: String, packageRequest: PackageRequest) {
            require(packageRequest.authorities.chain.containsKey(BusinessEntity.Manufacturer)) {
                "Manufacturer have to be specified"
            }

            val proof = getClaimProof(serial).state.data.proof
            val proofReq = getClaimProof(serial).state.data.proofReq.json

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
            val receiptSchema = IndyUser.SchemaDetails(
                    PackageReceipt.schemaName,
                    PackageReceipt.schemaVersion,
                    indyUser().did)

            val receiptProposal = PackageReceipt()
                    .addAttr(PackageReceipt.Attributes.Serial, serial)
                    .build()

            subFlow(IssueClaimFlow.Issuer(serial, receiptSchema, receiptProposal, patientAgent))

            flowSession.send(serial)
        }
    }
}