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

package com.luxoft.poc.supplychain.rpc

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger


/**
 * Demonstration of how to use the CordaRPCClient to connect to a Corda Node and
 * stream some State data from the node.
 */

open class RPCClient {
    companion object {
        private val logger: Logger = loggerFor<RPCClient>()
        private fun logState(state: Any) = logger.info("{}", state)

        @JvmStatic
        fun main(args: Array<String>) {

            for (node in args) {
                fillNode(node)
                examineLedger()
            }
        }

        private fun fillNode(node: String) {

            try {
                logger.info("Init process started for node: " + node)

                val client = CordaRPCClient(parse(node))

                // Can be amended in the com.template.MainKt file.
                val proxy = client.start("user1", "test").proxy

//                proxy.startFlow(CreateInitialInventoryFlow::Initiator,
//                        stuffForNode(proxy.nodeInfo())
//                ).returnValue.get()

                logger.info("Finished update for node: " + node)
            } catch (e: Exception) {
                logger.error("", e)
            }
        }


        private fun examineLedger() {
//                logger.info("Ledger state: ")

//                val (hotels, _) = proxy.vaultTrack(HotelState::class.java)
//                hotels.states.forEach { logState(it.state.data.hotel) }
//
//                val (roomtypes, _) = proxy.vaultTrack(RoomTypeState::class.java)
//                roomtypes.states.forEach { logState(it.state.data.roomType) }
//
//                val (rooms, _) = proxy.vaultTrack(RoomState::class.java)
//                rooms.states.forEach { logState(it.state.data.room) }
//
//                val (rates, _) = proxy.vaultTrack(RateState::class.java)
//                rates.states.forEach { logState(it.state.data.rate) }


//                logger.info("Subscribed to ledger updates.")
//
//                // Grab all signed transactions and all future signed transactions.
//                val (snapshot, updates) = proxy.vaultTrack(StuffState::class.java)
//
//                // Log the existing TemplateStates and listen for new ones.
//                snapshot.states.forEach { logState(it) }
//                updates.toBlocking().subscribe { update ->
//                    update.produced.forEach { logState(it) }
//                }
        }

    }
}
