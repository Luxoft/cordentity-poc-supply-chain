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

class DeliverShipmentFlowTests : ShipmentBase(NetworkConfiguration()) {

    @Test
    fun `states exist in Vault`() {

        newPackageRequest(config.treatment, config.issuer, packageInfo)
        runShipment(packageInfo.serial, config.issuer, config.carrier)

        val criteria = QueryCriteria.VaultQueryCriteria(status = ALL)

        config.issuer.database.transaction {
            val packageStates = config.issuer.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(2, packageStates.size)
            val byOwners = packageStates.groupingBy{ it.state.data.owner.owningKey }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty().owningKey])
            assertEquals(1, byOwners.eachCount()[config.carrier.getParty().owningKey])
            byOwners.sourceIterator().forEach { val data = it.state.data
                when(data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.carrier.getName() -> {
                        assertEquals(PackageState.PROCESSED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(config.issuer.getName(),    data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    else -> throw IllegalArgumentException("Unexpected package owner on Agent side: ${data.owner.nameOrNull()}")
                }
            }

            val shipmentStates = config.issuer.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.issuer.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.carrier.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.issuer.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.carrier.database.transaction {
            val packageStates = config.carrier.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packageStates.size)
            assertEquals(config.carrier.getParty(), packageStates[0].state.data.owner)
            assertEquals(PackageState.PROCESSED,    packageStates[0].state.data.info.state)
            assertEquals(config.issuer.getName(),   packageStates[0].state.data.info.issuedBy)
            assertEquals(config.treatment.getName(),packageStates[0].state.data.info.requestedBy)
            assertEquals(config.issuer.getName(),   packageStates[0].state.data.info.processedBy)

            val shipmentStates = config.carrier.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipmentStates.size)
            assertEquals(config.issuer.getParty(),  shipmentStates[0].state.data.from)
            assertEquals(config.carrier.getParty(), shipmentStates[0].state.data.to)

            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.carrier.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.agent.database.transaction {
            val packageStates = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(2, packageStates.size)
            val byOwners = packageStates.groupingBy{ it.state.data.owner.owningKey }
            assertEquals(1, byOwners.eachCount()[config.issuer.getParty().owningKey])
            assertEquals(1, byOwners.eachCount()[config.carrier.getParty().owningKey])
            byOwners.sourceIterator().forEach { val data = it.state.data
                when(data.owner.nameOrNull()) {
                    config.issuer.getName() -> {
                        assertEquals(PackageState.ISSUED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(null, data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    config.carrier.getName() -> {
                        assertEquals(PackageState.PROCESSED, data.info.state)
                        assertEquals(config.issuer.getName(),    data.info.issuedBy)
                        assertEquals(config.treatment.getName(), data.info.requestedBy)
                        assertEquals(config.issuer.getName(),    data.info.processedBy)
                        assertEquals(null, data.info.deliveredTo)
                    }
                    else -> throw IllegalArgumentException("Unexpected package owner on Agent side: ${data.owner.nameOrNull()}")
                }
            }

            val shipment = config.agent.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipment.size)
            assertEquals(config.issuer.getName(), shipment[0].state.data.from.nameOrNull())
            assertEquals(config.carrier.getName(), shipment[0].state.data.to.nameOrNull())

            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }

        config.treatment.database.transaction {
            val packageStates = config.treatment.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packageStates.size)
            assertEquals(config.issuer.getParty(), packageStates[0].state.data.owner)
            assertEquals(PackageState.ISSUED, packageStates[0].state.data.info.state)

            assertEquals(0, config.treatment.services.vaultService.queryBy<Shipment>(criteria).states.size)
            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.treatment.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }
    }
}
