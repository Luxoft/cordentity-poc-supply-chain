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
import com.luxoft.blockchainlab.corda.hyperledger.indy.Connection
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.*
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.IssueCredentialFlowB2C
import com.luxoft.blockchainlab.hyperledger.indy.SchemaId
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.flow.*
import net.corda.core.flows.*
import net.corda.core.serialization.CordaSerializable
import java.util.*


class AskNewPackage {

    @CordaSerializable
    data class PackageRequest(val patientDid: String)

    @InitiatingFlow
    @StartableByRPC
    open class Treatment : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val connection = serviceHub.cordaService(ConnectionService::class.java).getConnection()
            val packageRequest = PackageRequest(connection.getCounterParty()!!.did)

            try {
                val serial = UUID.randomUUID().toString()

                issueReceipt(serial, connection)
                requestNewPackage(serial, packageRequest)

            } catch (e: Exception) {
                logger.error("Patient cant be authenticated", e)
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun issueReceipt(serial: String, connection: Connection) {
            val receiptProposal = IndySchemaBuilder()
                    .addAttr(PackageReceipt.Attributes.Serial, serial)
                    .build()

            val schemaId = SchemaId(indyUser().did, PackageReceipt.schemaName, PackageReceipt.schemaVersion)
            val credDef = getCredentialDefinitionBySchemaId(schemaId)

            subFlow(IssueCredentialFlowB2C.Issuer(serial, receiptProposal, credDef!!.state.data.credentialDefinitionId, connection))
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
                    requestedBy = ourIdentity.name,
                    processedBy = getManufacturer().name
            )

            subFlow(RequestForPackage.Initiator(packageInfo))
        }
    }
}
