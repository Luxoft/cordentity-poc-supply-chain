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
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastToObservers(val observers: List<AbstractParty>,
                           val signedTrx: SignedTransaction) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() = observers.runSessions(this)
            .forEach { subFlow(SendTransactionFlow(it, signedTrx)) }
}
