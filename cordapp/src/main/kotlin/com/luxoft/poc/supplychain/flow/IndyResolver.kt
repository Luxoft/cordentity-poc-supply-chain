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
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIs
import com.luxoft.poc.supplychain.data.AuthorityInfo
import com.luxoft.poc.supplychain.data.AuthorityInfoMap
import com.luxoft.poc.supplychain.data.schema.CertificateIndySchema
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.unwrap

class IndyResolver {

    @InitiatingFlow
    @StartableByRPC
    class Requester(private val authorities: Map<String, CordaX500Name>) : FlowLogic<AuthorityInfoMap>() {

        @Suspendable
        override fun call(): AuthorityInfoMap {
            //TODO: change to state producing
            val response = AuthorityInfoMap()
            authorities.forEach { (key, name) ->
                response[key] = if (name == ourIdentity.name) {
                    getAuthorityInfo()
                } else {
                    val party = whoIs(name)
                    val session = initiateFlow(party)
                    val authorityInfo = session.receive<AuthorityInfo>().unwrap { it }
                    authorityInfo
                }
            }
            return response
        }
    }

    @InitiatedBy(Requester::class)
    class Responder(private val session: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            session.send(getAuthorityInfo())
        }
    }
}

fun FlowLogic<Any>.getAuthorityInfo(): AuthorityInfo {
    val indySchema = getIndySchemaLike(CertificateIndySchema.schemaName)
        ?: throw FlowException("We have multiple qualifying schemas")
    return AuthorityInfo(indyUser().walletUser.getIdentityDetails().did, indySchema.state.data.id.toString())
}
