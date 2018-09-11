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
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object RequestForPackage {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val packageInfo: PackageInfo, val manufacturerName: CordaX500Name) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val manufacturer = whoIs(manufacturerName)

            val observers = listOf<AbstractParty>(whoIs(packageInfo.patientAgent))
            val signers = listOf(manufacturer, ourIdentity)

            // TODO: we skipped production phase. Status should be Pending
            val info = packageInfo.copy(
                    state = PackageState.ISSUED,
                    issuedAt = System.currentTimeMillis(),
                    issuedBy = manufacturerName)

            // Initiator should be in list of participant on Issuing phase, only on pending
            // However we skipped real package issuing.
            val newPackage = Package(info, manufacturer, observers, listOf(manufacturer, ourIdentity))
            val newPackageOut = StateAndContract(newPackage, PackageContract::class.java.name)
            val newPackageCmd = Command(PackageContract.Request(), signers.mapToKeys())

            val flowSessions = signers.except(ourIdentity).runSessions(this)

            val trxBuilder = TransactionBuilder(whoIsNotary()).withItems(newPackageOut, newPackageCmd)
            val selfSignedTrx = serviceHub.signInitialTransaction(trxBuilder, ourIdentity.owningKey)
            val signedTrx = subFlow(CollectSignaturesFlow(selfSignedTrx, flowSessions))

            val finalTrx = subFlow(FinalityFlow(signedTrx))

            waitForLedgerCommit(finalTrx.id)
            subFlow(BroadcastToObservers(observers, finalTrx))
        }
    }

    @InitiatedBy(Initiator::class)
    class Counterparty(val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val flow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) = Unit
            }
            subFlow(flow)
        }
    }

}
