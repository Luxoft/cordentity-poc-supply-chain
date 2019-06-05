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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIsNotary
import com.luxoft.blockchainlab.hyperledger.indy.utils.proofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.reveal
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.data.state.getParties
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import com.luxoft.poc.supplychain.service.clientResolverService
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import java.util.*


class PackageWithdrawal {

    @InitiatingFlow
    @StartableByRPC
    class Owner(val serial: String, val clientId: UUID) : FlowLogic<Unit>() {
        val clientDid by lazy { clientResolverService().userUuid2Did[clientId]!! }

        @Suspendable
        override fun call() {
            try {
                verifyReceipt()
                withdrawPackage()
            } catch (e: Exception) {
                logger.error("Patient cant be authenticated", e)
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun verifyReceipt() {
            //TODO: Verify issuer,schema and serial
            val serialProofRequest = proofRequest("proof_req", "1.0") {
                reveal("serial") // { FilterProperty.Value shouldBe serial }
            }
            subFlow(VerifyCredentialFlowB2C.Verifier(serial, clientDid, serialProofRequest))
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
