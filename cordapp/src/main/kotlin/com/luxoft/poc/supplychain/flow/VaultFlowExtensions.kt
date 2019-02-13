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
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.schema.*
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyCredential
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyCredentialProof
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIs
import com.luxoft.poc.supplychain.IndyArtifactsRegistry
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.IndySchema
import com.luxoft.poc.supplychain.data.schema.PackageSchemaV1
import com.luxoft.poc.supplychain.data.schema.ShipmentSchemaV1
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.data.state.Shipment
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria

fun FlowLogic<Any>.getTreatment() = whoIs(CordaX500Name("TreatmentCenter", "London", "GB"))
fun FlowLogic<Any>.getManufacturer() = whoIs(CordaX500Name("Manufacture", "London", "GB"))
fun FlowLogic<Any>.getSovrinAgent() = whoIs(CordaX500Name("SovrinAgent", "London", "GB"))

fun FlowLogic<Any>.getPackageState(serial: String, owner: AbstractParty): StateAndRef<Package> {
    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

    val serial = QueryCriteria.VaultCustomQueryCriteria(
            PackageSchemaV1.PersistentPackage::serial.equal(serial))

    val owner = QueryCriteria.VaultCustomQueryCriteria(
            PackageSchemaV1.PersistentPackage::owner.equal(owner.owningKey.encoded))

    val criteria = generalCriteria.and(serial).and(owner)

    val results = serviceHub.vaultService.queryBy<Package>(criteria)
    return results.states.singleOrNull()!!
}

fun FlowLogic<Any>.getPackageState(serial: String, status: PackageState): StateAndRef<Package> {
    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)

    val serial = QueryCriteria.VaultCustomQueryCriteria(
            PackageSchemaV1.PersistentPackage::serial.equal(serial))

    val state = QueryCriteria.VaultCustomQueryCriteria(
            PackageSchemaV1.PersistentPackage::state.equal(status.ordinal))

    val criteria = generalCriteria.and(serial).and(state)

    val results = serviceHub.vaultService.queryBy<Package>(criteria)
    return results.states.singleOrNull()!!
}

fun FlowLogic<Any>.getShipmentState(serial: String, isConsumed: Boolean = false): StateAndRef<Shipment> {
    val consumeStatus = if(isConsumed) Vault.StateStatus.CONSUMED else Vault.StateStatus.UNCONSUMED
    val generalCriteria = QueryCriteria.VaultQueryCriteria(consumeStatus)
    val serial = QueryCriteria.VaultCustomQueryCriteria(ShipmentSchemaV1.PersistentShipment::serial.equal(serial))

    val criteria = generalCriteria.and(serial)

    val results = serviceHub.vaultService.queryBy<Shipment>(criteria)
    return results.states.singleOrNull()!!
}

fun FlowLogic<Any>.getClaimFrom(serial: String, issuer: String): StateAndRef<IndyCredential> {

    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
    val issuer = QueryCriteria.VaultCustomQueryCriteria(CredentialSchemaV1.PersistentCredential::issuerDid.equal(issuer))
    val serial = QueryCriteria.VaultCustomQueryCriteria(CredentialSchemaV1.PersistentCredential::id.equal(serial))

    val criteria = generalCriteria.and(issuer).and(serial)

    val results = serviceHub.vaultService.queryBy<IndyCredential>(criteria)
    return results.states.singleOrNull()!!
}

fun FlowLogic<Any>.getClaimProof(serial: String): StateAndRef<IndyCredentialProof> {

    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
    val serial = QueryCriteria.VaultCustomQueryCriteria(CredentialProofSchemaV1.PersistentProof::id.equal(serial))

    val criteria = generalCriteria.and(serial)

    val results = serviceHub.vaultService.queryBy<IndyCredentialProof>(criteria)
    return results.states.singleOrNull()!!
}