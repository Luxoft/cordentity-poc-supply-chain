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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.IssueCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialValue
import com.luxoft.blockchainlab.hyperledger.indy.utils.*
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.PackageIndySchema
import com.luxoft.poc.supplychain.flow.*
import com.luxoft.poc.supplychain.flow.GetInviteFlow.Companion.inviteWaitTimeout
import com.luxoft.poc.supplychain.service.clientResolverService
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.serialization.CordaSerializable
import java.util.*


class AskNewPackage {

    @CordaSerializable
    data class PackageRequest(val patientDid: String)

    @InitiatingFlow
    @StartableByRPC
    open class Treatment(clientId: UUID, private val trustedCredentialsIssuerDID: String) : FlowLogic<Unit>() {
        val clientDid by lazy { clientResolverService().userUuid2Did[clientId]!!.get(inviteWaitTimeout) }

        @Suspendable
        override fun call() {
            val packageRequest = PackageRequest(clientDid)

            try {
                val serial = UUID.randomUUID().toString()

                checkPermissions()

                issueReceipt(serial)
                requestNewPackage(serial, packageRequest)

            } catch (e: Throwable) {
                logger.error("Patient cant be authenticated", e)
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun checkPermissions() {
            val proofRequest = proofRequest("user_proof_req", "1.0") {
                reveal("name")
                reveal("sex")
                reveal("medical id") { FilterProperty.IssuerDid shouldBe trustedCredentialsIssuerDID }
                reveal("medical condition") {
                    //                    FilterProperty.Value shouldBe "Healthy"
                    FilterProperty.IssuerDid shouldBe trustedCredentialsIssuerDID
                }
                proveGreaterThan("age", 18)
            }
            //In case of ignoring verification
//            connectionService().sendProofRequest(proofRequest, clientDid)
            if (!subFlow(VerifyCredentialFlowB2C.Verifier(clientDid, clientDid, proofRequest)))
                throw throw FlowException("Permission verification failed")
        }

        @Suspendable
        private fun issueReceipt(serial: String) {

            val authorities = subFlow(
                IndyResolver.Requester(
                    mapOf(
                        "Manufacture" to getManufacturer().name,
                        "TRC" to ourIdentity.name
                    )
                )
            )

            val credDefId = getCredDefLike(PackageIndySchema.schemaName)!!.state.data.id

            subFlow(IssueCredentialFlowB2C.Issuer(serial, credDefId, null, clientDid) {
                attributes["serial"] = CredentialValue(serial)
                attributes["authorities"] = CredentialValue(SerializationUtils.anyToJSON(authorities))
                attributes["time"] = CredentialValue(System.currentTimeMillis().toString())
            })
        }

        @Suspendable
        private fun requestNewPackage(serial: String, packageRequest: PackageRequest) {
            // create new package request
            val packageInfo = PackageInfo(
                    serial = serial,
                    state = PackageState.NEW,
                    patientDid = packageRequest.patientDid,
                    patientDiagnosis = "leukemia",
                    medicineName = "Santorium",
                    medicineDescription = "package-required",
                    requestedAt = System.currentTimeMillis(),
                    requestedBy = ourIdentity.name,
                    processedBy = getManufacturer().name
            )

            subFlow(RequestForPackage.Initiator(packageInfo))
        }
    }
}
