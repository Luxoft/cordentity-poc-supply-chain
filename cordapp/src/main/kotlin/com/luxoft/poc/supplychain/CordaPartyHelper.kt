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

package com.luxoft.poc.supplychain

import net.corda.core.flows.FlowLogic
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

fun List<AbstractParty>.except(who: AbstractParty) = this.filter { it != who }
fun List<AbstractParty>.except(who: List<AbstractParty>) = this.filter { !who.contains(it) }
fun List<AbstractParty>.mapToKeys() = this.map { it.owningKey }

fun List<AbstractParty>.runSessions(flow: FlowLogic<Any>) = this.map { flow.initiateFlow(it as Party) }
