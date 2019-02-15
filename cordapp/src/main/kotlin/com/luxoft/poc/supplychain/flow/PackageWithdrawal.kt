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

package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.Connection
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.ProofAttribute
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.getCredentialDefinitionBySchemaId
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIsNotary
import com.luxoft.blockchainlab.hyperledger.indy.SchemaId
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.data.state.getParties
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder


class PackageWithdrawal {

    @InitiatingFlow
    @StartableByRPC
    class Owner(val serial: String) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            try {
                val connection = serviceHub.cordaService(ConnectionService::class.java).getConnection()

                verifyReceipt(connection)
                withdrawPackage()
            } catch (e: Exception) {
                logger.error("Patient cant be authenticated", e)
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun verifyReceipt(connection: Connection) {
            val schemaId = SchemaId(indyUser().did, PackageReceipt.schemaName, PackageReceipt.schemaVersion)
            val credDef = getCredentialDefinitionBySchemaId(schemaId)

            val serialProof = ProofAttribute(schemaId, credDef!!.state.data.credentialDefinitionId, "serial", serial)
            subFlow(VerifyCredentialFlowB2C.Verifier(serial, listOf(serialProof), emptyList(), null, connection))
        }

        @Suspendable
        private fun withdrawPackage() {
            val packageIn = getPackageState(serial, PackageState.DELIVERED)

            val info = packageIn.getInfo().copy(
                    state = PackageState.COLLECTED,
                    collectedAt = System.currentTimeMillis())

            val collectedPackage = packageIn.state.data.collect(ourIdentity, info)
            val packageOut = StateAndContract(collectedPackage, PackageContract::class.java.name)
            val packageCmd = Command(PackageContract.Collect(), packageIn.getParties().mapToKeys())

            val trxBuilder = TransactionBuilder(whoIsNotary())
                    .withItems(
                            packageIn,
                            packageOut,
                            packageCmd
                    )

            val flowSessions = packageIn.getParties()
                    .except(ourIdentity)
                    .runSessions(this)

            val selfSignedTx = serviceHub.signInitialTransaction(trxBuilder, ourIdentity.owningKey)
            val signedTrx = subFlow(CollectSignaturesFlow(selfSignedTx, flowSessions))
            val finalTrx = subFlow(FinalityFlow(signedTrx))

            waitForLedgerCommit(finalTrx.id)
        }
    }
}
