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

package com.luxoft.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaim
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaimProof
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.data.state.Shipment
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.Vault.StateStatus.ALL
import org.junit.Test
import kotlin.test.assertEquals

class ReceiveShipmentFlowTests : ShipmentBase(NetworkConfiguration()) {

    @Test
    fun `short chain receiver-issuer-receiver `() {
        newPackageRequest(config.treatment, config.issuer, packageInfo)

        runShipment(packageInfo.serial, config.issuer, config.treatment)
        endShipment(packageInfo.serial, config.treatment)

        val criteria = QueryCriteria.VaultQueryCriteria(status = ALL)

        config.agent.database.transaction {
            val packageStates = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(3, packageStates.size)

            val byOwners = packageStates.groupingBy{ it.state.data.owner }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty()])
            assertEquals(2, byOwners.eachCount()[config.treatment.getParty()])
            byOwners.sourceIterator().forEach { val data = it.state.data
                when(data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.treatment.getName() -> {
                        when(data.info.state) {
                            PackageState.DELIVERED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(),    data.info.issuedBy)
                                assertEquals(config.issuer.getName(),    data.info.processedBy)
                                assertEquals(config.treatment.getName(), data.info.deliveredTo)
                            }
                            PackageState.PROCESSED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(),    data.info.issuedBy)
                                assertEquals(config.issuer.getName(),    data.info.processedBy)
                                assertEquals(null, data.info.deliveredTo)
                            }
                            else -> throw IllegalArgumentException("Invalid state of package on Agent Side: ${data.info.state}")
                        }
                    }
                    else -> throw IllegalArgumentException("Unexpected package owner on Agent side: ${data.owner.nameOrNull()}")
                }
            }

            val shipment = config.agent.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipment.size)
            assertEquals(config.issuer.getParty(),  shipment[0].state.data.from)
            assertEquals(config.treatment.getParty(), shipment[0].state.data.to)

            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.issuer.database.transaction {
            val packageStates = config.issuer.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(2, packageStates.size)
            val byOwners = packageStates.groupingBy{ it.state.data.owner }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty()])
            assertEquals(1, byOwners.eachCount()[config.treatment.getParty()])
            byOwners.sourceIterator().forEach { val data = it.state.data
                when(data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.treatment.getName() -> {
                        assertEquals(PackageState.PROCESSED, data.info.state)
                            assertEquals(config.treatment.getName(), data.info.requestedBy)
                            assertEquals(config.issuer.getName(),    data.info.issuedBy)
                            assertEquals(config.issuer.getName(),    data.info.processedBy)
                            assertEquals(null, data.info.deliveredTo)
                    }
                }
            }

            val shipmentStates = config.issuer.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.issuer.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.treatment.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.treatment.database.transaction {
            val packageStates = config.treatment.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(3, packageStates.size)
            val byOwners = packageStates.groupingBy { it.state.data.owner }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty()])
            assertEquals(2, byOwners.eachCount()[config.treatment.getParty()])
            byOwners.sourceIterator().forEach {
                val data = it.state.data
                when (data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(), data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.treatment.getName() -> {
                        when (data.info.state) {
                            PackageState.DELIVERED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.issuer.getName(), data.info.processedBy)
                                assertEquals(config.treatment.getName(), data.info.deliveredTo)
                            }
                            PackageState.PROCESSED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.issuer.getName(), data.info.processedBy)
                                assertEquals(null, data.info.deliveredTo)
                            }
                            else -> throw IllegalArgumentException("Invalid state of package on Agent Side: ${data.info.state}")
                        }
                    }
                    else -> throw IllegalArgumentException("Unexpected package owner on Agent side: ${data.owner.nameOrNull()}")
                }
            }
            val shipmentStates = config.treatment.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.issuer.getParty(), shipmentStates[0].state.data.from)
            assertEquals(config.treatment.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }
    }

    @Test
    fun `long chain receiver-issuer-carrier-government-receiver`() {

        newPackageRequest(config.treatment, config.issuer, packageInfo)

        runShipment(packageInfo.serial, config.issuer, config.carrier)
        endShipment(packageInfo.serial, config.carrier)

        runShipment(packageInfo.serial, config.carrier, config.goverment)
        endShipment(packageInfo.serial, config.goverment)

        runShipment(packageInfo.serial, config.goverment, config.treatment)
        endShipment(packageInfo.serial, config.treatment)

        val criteria = QueryCriteria.VaultQueryCriteria(status = ALL)

        config.agent.database.transaction {
            val packageStates = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(7, packageStates.size)
            val byOwners = packageStates.groupingBy { it.state.data.owner }
            assertEquals(2, byOwners.eachCount()[config.treatment.getParty()])
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty()])
            assertEquals(2, byOwners.eachCount()[config.carrier.getParty()])
            assertEquals(2, byOwners.eachCount()[config.goverment.getParty()])

            byOwners.sourceIterator().forEach {
                val data = it.state.data
                when (data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(), data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.treatment.getName() -> {
                        when (data.info.state) {
                            PackageState.DELIVERED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.goverment.getName(), data.info.processedBy)
                                assertEquals(config.treatment.getName(), data.info.deliveredTo)
                            }
                            PackageState.PROCESSED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.goverment.getName(), data.info.processedBy)
                                assertEquals(config.goverment.getName(), data.info.deliveredTo)
                            }
                            else -> throw IllegalArgumentException("Invalid state of package on Agent Side: ${data.info.state}")
                        }
                    }
                    config.carrier.getName() -> {
                        when (data.info.state) {
                            PackageState.DELIVERED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.issuer.getName(), data.info.processedBy)
                                assertEquals(config.carrier.getName(), data.info.deliveredTo)
                            }
                            PackageState.PROCESSED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.issuer.getName(), data.info.processedBy)
                                assertEquals(null, data.info.deliveredTo)
                            }
                            else -> throw IllegalArgumentException("Invalid state of package on Agent Side: ${data.info.state}")
                        }
                    }
                    config.goverment.getName() -> {
                        when (data.info.state) {
                            PackageState.DELIVERED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.carrier.getName(), data.info.processedBy)
                                assertEquals(config.goverment.getName(), data.info.deliveredTo)
                            }
                            PackageState.PROCESSED -> {
                                assertEquals(config.treatment.getName(), data.info.requestedBy)
                                assertEquals(config.issuer.getName(), data.info.issuedBy)
                                assertEquals(config.carrier.getName(), data.info.processedBy)
                                assertEquals(config.carrier.getName(), data.info.deliveredTo)
                            }
                            else -> throw IllegalArgumentException("Invalid state of package on Agent Side: ${data.info.state}")
                        }
                    }
                    else -> throw IllegalArgumentException("Unexpected package owner on Agent side: ${data.owner.nameOrNull()}")
                }
            }

            assertEquals(3, config.agent.services.vaultService.queryBy<Shipment>(criteria).states.size)
            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.issuer.database.transaction {
            val packageStates = config.issuer.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(2, packageStates.size)
            val byOwners = packageStates.groupingBy { it.state.data.owner }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty()])
            assertEquals(1, byOwners.eachCount()[config.carrier.getParty()])

            byOwners.sourceIterator().forEach { val data = it.state.data
                when (data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(config.issuer.getName(), data.info.issuedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.carrier.getName() -> {
                        assertEquals(PackageState.PROCESSED, data.info.state)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(config.issuer.getName(), data.info.issuedBy)
                        assertEquals(config.issuer.getName(), data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    else -> throw IllegalArgumentException("Invalid state of package on Issuer Side: ${data.info.state}")
                }
            }

            val shipmentStates = config.issuer.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.issuer.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.carrier.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }
/*
        config.carrier.database.transaction {
            val packageStates = config.carrier.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(3, packageStates.size)

            val packageGroupByOwners = packageStates.groupingBy{ it.state.data.owner }.eachCount()
            assertEquals(1, packageGroupByOwners[config.issuer.getParty()])
            assertEquals(2, packageGroupByOwners[config.carrier.getParty()])

            val shipmentStates = config.carrier.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(2, shipmentStates.size)
            assertEquals(config.issuer.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.carrier.getParty(), shipmentStates[0].state.data.to)

            assertEquals(config.carrier.getParty(),  shipmentStates[1].state.data.from)
            assertEquals(config.goverment.getParty(), shipmentStates[1].state.data.to)

            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.treatment.database.transaction {
            val packageStates = config.treatment.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(5, packageStates.size)

            val packageGroupByOwners = packageStates.groupingBy{ it.state.data.owner }.eachCount()
            assertEquals(1, packageGroupByOwners[config.carrier.getParty()])
            //TODO: assertEquals(1, packageGroupByOwners[config.issuer.getParty()])
            assertEquals(2, packageGroupByOwners[config.goverment.getParty()])
            assertEquals(1, packageGroupByOwners[config.treatment.getParty()])

            val shipmentStates = config.treatment.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.goverment.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.treatment.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }
       */
    }
}
