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
 *//*


package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.service.IndyService
import net.corda.core.identity.CordaX500Name
import net.corda.node.internal.StartedNode
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.internal.InternalMockNetwork

fun StartedNode<InternalMockNetwork.MockNode>.getParty() = this.info.singleIdentity()

fun StartedNode<InternalMockNetwork.MockNode>.getName() = getParty().name

fun StartedNode<InternalMockNetwork.MockNode>.getPubKey() = getParty().owningKey

fun CordaX500Name.getNodeByName(net: InternalMockNetwork) =
        net.defaultNotaryNode.services.identityService.wellKnownPartyFromX500Name(this)!!

fun StartedNode<InternalMockNetwork.MockNode>.getPartyDid() =
        this.services.cordaService(IndyService::class.java).indyUser.did
*/
