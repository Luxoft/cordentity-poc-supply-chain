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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.VerifyCredentialFlowB2C
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.awaitFiber
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.connectionService
import com.luxoft.blockchainlab.hyperledger.indy.models.Interval
import com.luxoft.blockchainlab.hyperledger.indy.utils.proofRequest
import com.luxoft.blockchainlab.hyperledger.indy.utils.proveNonRevocation
import com.luxoft.blockchainlab.hyperledger.indy.utils.reveal
import com.luxoft.poc.supplychain.flow.getManufacturer
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class GetPackageHistory {

    @InitiatingFlow
    @StartableByRPC
    open class Requester(val serial: String) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            val session = initiateFlow(getManufacturer())
            val invite = session.sendAndReceive<String>(serial).unwrap { data -> data }
            return invite
        }
    }

    @InitiatedBy(Requester::class)
    class HistoryOwner(private val session: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val serial = session.receive<String>().unwrap { it }
            val invite = connectionService().getConnection().generateInvite().awaitFiber()
            session.send(invite)
            connectionService().getConnection().waitForInvitedParty(invite, 300000).awaitFiber().apply {
                checkPermissions(serial, partyDID())

                do {
                    val proofRequest = try {
                        receiveProofRequest().timeout(30, TimeUnit.SECONDS).toBlocking().value()
                    } catch (e: RuntimeException) {
                        //End of waiting for proof requests
                        if (e.cause !is TimeoutException)
                            throw e
                        null
                    }?.also {
                        val proof = indyUser().createProofFromLedgerData(it)

                        sendProof(proof)
                    }
                } while (proofRequest != null)
            }
        }

        @Suspendable
        private fun checkPermissions(serial: String, partyDid: String) {
            val proofRequest = proofRequest("user_proof_req", "1.0") {
                reveal("serial") { "serial" shouldBe serial }
                proveNonRevocation(Interval.allTime())
            }
            if (!subFlow(VerifyCredentialFlowB2C.Verifier(partyDid, partyDid, proofRequest)))
                throw throw FlowException("Permission verification failed")
        }

    }
}
