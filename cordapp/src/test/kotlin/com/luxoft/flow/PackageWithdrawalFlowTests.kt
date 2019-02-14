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

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaim
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaimProof
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.DiagnosisDetails
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.data.state.Shipment
import com.luxoft.poc.supplychain.flow.PackageWithdrawal
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.internal.startFlow
import kotlin.test.assertEquals

class PackageWithdrawalFlowTests: ShipmentBase(NetworkConfiguration()) {

    //@Test
    fun `user not authorized to withdraw the package`() {

    }

    //@Test
    fun `user authorized to withdraw the package`() {
        val meta = listOf<CredentialDesc>(
                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(PackageReceipt.Attributes.Serial, packageInfo.serial)
                                .build(), getCredDefId(config.treatment, PackageReceipt), config.treatment),

                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(DiagnosisDetails.Attributes.Stage, "4")
                                .addAttr(DiagnosisDetails.Attributes.Disease, "leukemia")
                                .addAttr(DiagnosisDetails.Attributes.MedicineName, "package-name")
                                .addAttr(DiagnosisDetails.Attributes.Recommendation, "package-required")
                                .build(), getCredDefId(config.insurance, DiagnosisDetails), config.insurance),

                CredentialDesc(
                        IndySchemaBuilder()
                                .addAttr(PersonalInformation.Attributes.Age, "20")
                                .addAttr(PersonalInformation.Attributes.Nationality, "eu")
                                .addAttr(PersonalInformation.Attributes.Forename, "Mike J")
                                .build(), getCredDefId(config.goverment, PersonalInformation), config.goverment)
        )

        meta.forEach { issueClaim(it, config.agent) }

        newPackageRequest(config.treatment, config.issuer, packageInfo)

        runShipment(packageInfo.serial, config.issuer, config.treatment)
        endShipment(packageInfo.serial, config.treatment)

        val flowPackageWithdrawal = PackageWithdrawal.Owner(packageInfo.serial)
        val packageWithdrawalFuture = config.agent.services.startFlow(flowPackageWithdrawal).resultFuture

        config.runNetwork()
        packageWithdrawalFuture.getOrThrow()

        config.agent.database.transaction {
            // TODO: Check claims and Proof

            var criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)

            var packageStates = config.agent.services.vaultService.queryBy<Package>(criteria).states
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

            criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)

            packageStates = config.agent.services.vaultService.queryBy<Package>(criteria).states
            assertEquals(1, packageStates.size)
            assertEquals(PackageState.COLLECTED, packageStates[0].state.data.info.state)
            assertEquals(config.treatment.getName(), packageStates[0].state.data.info.requestedBy)
            assertEquals(config.issuer.getName(),    packageStates[0].state.data.info.issuedBy)
            assertEquals(config.issuer.getName(),    packageStates[0].state.data.info.processedBy)
            assertEquals(config.treatment.getName(), packageStates[0].state.data.info.deliveredTo)


            criteria = QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.CONSUMED)

            val shipment = config.agent.services.vaultService.queryBy<Shipment>(criteria).states
            assertEquals(1, shipment.size)
            assertEquals(config.issuer.getParty(),  shipment[0].state.data.from)
            assertEquals(config.treatment.getParty(), shipment[0].state.data.to)

            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaim>(criteria).states.size)
            assertEquals(0, config.agent.services.vaultService.queryBy<IndyClaimProof>(criteria).states.size)
        }
    }
}
*/
