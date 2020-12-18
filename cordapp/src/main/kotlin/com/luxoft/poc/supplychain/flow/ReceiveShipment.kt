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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2b.IssueCredentialFlowB2B
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIsNotary
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialValue
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.contract.ShipmentContract
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.CertificateIndySchema
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.data.state.getParties
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object ReceiveShipment {

    @InitiatingFlow
    @StartableByRPC
    class Receiver(private val acceptanceCheck: AcceptanceResult) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val packageIn = getPackageState(acceptanceCheck.serial, ourIdentity)
            val shipmentIn = getShipmentState(acceptanceCheck.serial)

            val packageOwners = packageIn.getParties()
            val flowSessionsToPackageOwners = packageOwners
                    .except(ourIdentity)
                    .runSessions(this)

            val dealParticipants = shipmentIn.getParties()
            val flowSessionsToDealParticipants = dealParticipants
                    .except(ourIdentity)
                    .except(packageOwners)
                    .runSessions(this)

            val shipmentCmd =
                    if(acceptanceCheck.isAccepted)
                        Command(ShipmentContract.Accept(), dealParticipants.mapToKeys())
                    else
                        Command(ShipmentContract.Decline(), dealParticipants.mapToKeys())

            val info = packageIn.getInfo().copy(
                    state = PackageState.DELIVERED,
                    deliveredAt = System.currentTimeMillis(),
                    deliveredTo = ourIdentity.name)
            val deliveredPackage = packageIn.state.data.ship(ourIdentity, info)

            val packageOut = StateAndContract(deliveredPackage, PackageContract::class.java.name)
            val packageCmd = Command(PackageContract.CompleteShipment(), packageIn.getParties().mapToKeys())

            val trxBuilder = TransactionBuilder(whoIsNotary()).withItems(
                    packageIn,
                    packageOut,
                    packageCmd,
                    shipmentIn,
                    shipmentCmd
            )

            val flowSessions = flowSessionsToPackageOwners.union(flowSessionsToDealParticipants)

            val selfSignedTx = serviceHub.signInitialTransaction(trxBuilder, ourIdentity.owningKey)
            val signedTrx = subFlow(CollectSignaturesFlow(selfSignedTx, flowSessions))
            val finalTrx = subFlow(FinalityFlow(signedTrx))

            val revocationRegistry = getRevocationRegistryLike(CertificateIndySchema.schemaName)!!.state.data
            subFlow(
                IssueCredentialFlowB2B.Issuer(
                    getManufacturer().name,
                    revocationRegistry.credentialDefinitionId,
                    revocationRegistry.id
                ) {
                attributes["serial"] = CredentialValue(acceptanceCheck.serial)
                attributes["status"] = CredentialValue(info.state.name)
                attributes["time"] = CredentialValue(info.processedAt!!.toString())
            })

            waitForLedgerCommit(finalTrx.id)
        }
    }

    @InitiatedBy(Receiver::class)
    class Counterparty(private val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) = Unit
            }
            subFlow(signTransactionFlow)
        }
    }
}
