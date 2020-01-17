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

package com.luxoft.lumedic.ssi.corda.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.finalizeTransaction
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.me
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIsNotary
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.awaitFiber
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.connectionService
import com.luxoft.blockchainlab.hyperledger.indy.utils.Filter
import com.luxoft.blockchainlab.hyperledger.indy.utils.FilterProperty
import com.luxoft.blockchainlab.hyperledger.indy.utils.proofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.reveal
import com.luxoft.lumedic.ssi.corda.contract.ToDoContract
import com.luxoft.lumedic.ssi.corda.data.AuthResponse
import com.luxoft.lumedic.ssi.corda.data.AuthState
import com.luxoft.lumedic.ssi.corda.data.state.AuthProcessState
import com.luxoft.lumedic.ssi.corda.service.epicCommunicationService
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import java.util.*


class AuthPatient {
    @InitiatingFlow
    @StartableByRPC
    open class Hospital(val credentialsIssuerDid: String, val requestId: String?) : FlowLogic<AuthResponse>() {
        constructor(issuerDid: String) : this(issuerDid, null)

        companion object {
            object FLOW_INIT : ProgressTracker.Step("Initialising flow.")
            object GET_INVITE : ProgressTracker.Step("Requesting agent invite.")
            object VERIFYING_TX : ProgressTracker.Step("Verifying contract constraints.")
            object FINALISING_TX : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                FLOW_INIT,
                GET_INVITE,
                VERIFYING_TX,
                FINALISING_TX
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): AuthResponse {
            progressTracker.currentStep = FLOW_INIT
            val me = me()
            try {
                val invite = connectionService().getConnection().generateInvite().awaitFiber()
                val requestId = requestId ?: UUID.nameUUIDFromBytes(invite.toByteArray()).toString()

                val authResponse = AuthResponse(requestId, invite)
                val authProcessState = AuthProcessState(authResponse, AuthState.INVITED, null, me())
                val transactionBuilder =
                    TransactionBuilder(whoIsNotary()).addCommand(ToDoContract.Commands.Do(), me.owningKey)
                        .addOutputState(authProcessState)

                progressTracker.currentStep = VERIFYING_TX
                transactionBuilder.verify(serviceHub)

                progressTracker.currentStep = FINALISING_TX
                val notarizedTx = finalizeTransaction(serviceHub.signInitialTransaction(transactionBuilder))
                waitForLedgerCommit(notarizedTx.id)

                val flowSession = initiateFlow(me)
                flowSession.send(authResponse)
                flowSession.send(credentialsIssuerDid)
                flowSession.send(notarizedTx.coreTransaction.outRefsOfType(AuthProcessState::class.java).single().ref)
                return authResponse
            } catch (e: Throwable) {
                logger.error("Error in ${this.javaClass.canonicalName}", e)
                throw FlowException(e.message)
            }
        }
    }

    @InitiatedBy(Hospital::class)
    class AsyncProcess(private val flowSession: FlowSession) : FlowLogic<Unit>() {

        companion object {
            object FLOW_INIT : ProgressTracker.Step("Initialising flow.")
            object PATIENT_CONNECTION : ProgressTracker.Step("Waiting for connection to patient.")
            object VERIFY_CREDS : ProgressTracker.Step("Verifying credentials.")
            object UPDATE_EPIC : ProgressTracker.Step("Updating EPIC record.")

            fun tracker() = ProgressTracker(
                FLOW_INIT,
                PATIENT_CONNECTION,
                VERIFY_CREDS,
                UPDATE_EPIC
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call() {
            progressTracker.currentStep = FLOW_INIT
            val me = me()
            if (flowSession.counterparty != me)
                throw FlowException("Flow started by ${flowSession.counterparty}, not by $me")
            val authResponse = flowSession.receive<AuthResponse>().unwrap { it }
            val credentialsIssuerDid = flowSession.receive<String>().unwrap { it }
            val authStateRef = flowSession.receive<StateRef>().unwrap { it }
            var authProcessStateAndRef = serviceHub.toStateAndRef<AuthProcessState>(authStateRef)
            try {
                progressTracker.currentStep = PATIENT_CONNECTION
                val patientDid =
                    connectionService().getConnection().waitForInvitedParty(authResponse.invite, 300000).awaitFiber()
                        .partyDID()
                authProcessStateAndRef =
                    setAuthState(authProcessStateAndRef, AuthState.CONNECTED, ToDoContract.Commands.Do())

                progressTracker.currentStep = VERIFY_CREDS
                checkPermissions(authResponse.requestId, credentialsIssuerDid, patientDid)
                authProcessStateAndRef =
                    setAuthState(authProcessStateAndRef, AuthState.SUCCESS, ToDoContract.Commands.Do())

                progressTracker.currentStep = UPDATE_EPIC
                epicCommunicationService().postData("")
            } catch (e: Exception) {
                logger.error("Error in ${this.javaClass.canonicalName}", e)
                setAuthState(authProcessStateAndRef, AuthState.FAILED, ToDoContract.Commands.Do())
                throw FlowException(e.message)
            }
        }

        @Suspendable
        private fun checkPermissions(requestId: String, credentialsIssuerDid: String, patientDid: String) {
            val constraints: (Filter.() -> Unit) =
                { FilterProperty.IssuerDid shouldBe credentialsIssuerDid }
            val proofRequest = proofRequest("user_proof_req", "1.0") {
                reveal("Insurance_company_name", constraints)
                reveal("Insurance/member_ID", constraints)
                reveal("Insurance_effective_date_ms", constraints)
                reveal("Covered_through_ms", constraints)
                reveal("Copayment_amount", constraints)
                reveal("Full_legal_name", constraints)
                reveal("SSN", constraints)
                reveal("Date_of_birth_ms", constraints)
            }
            if (!subFlow(VerifyCredentialFlowB2C.Verifier(requestId, patientDid, proofRequest)))
                throw throw FlowException("Permission verification failed")
        }
    }
}

@Suspendable
fun FlowLogic<Any>.setAuthState(
    onState: StateAndRef<AuthProcessState>,
    authState: AuthState,
    command: ToDoContract.Commands
): StateAndRef<AuthProcessState> {
    val transactionBuilder = TransactionBuilder(whoIsNotary()).addCommand(command, me().owningKey)
        .addInputState(onState)
        .addOutputState(onState.state.data.copy(authState = authState))

    transactionBuilder.verify(serviceHub)

    val notarizedTx = finalizeTransaction(serviceHub.signInitialTransaction(transactionBuilder))
    waitForLedgerCommit(notarizedTx.id)

    return notarizedTx.coreTransaction.outRefsOfType(AuthProcessState::class.java).single()
}
