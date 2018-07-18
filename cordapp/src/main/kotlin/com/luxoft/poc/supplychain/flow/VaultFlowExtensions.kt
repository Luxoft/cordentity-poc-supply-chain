package com.luxoft.poc.supplychain.flow

import com.luxoft.blockchainlab.corda.hyperledger.indy.data.schema.ClaimProofSchemaV1
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.schema.ClaimSchemaV1
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaim
import com.luxoft.blockchainlab.corda.hyperledger.indy.data.state.IndyClaimProof
import com.luxoft.poc.supplychain.data.PackageState
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

fun FlowLogic<Any>.whoIsNotary(): Party {
    return serviceHub.networkMapCache.notaryIdentities.single()
}

fun FlowLogic<Any>.whoIs(x509: CordaX500Name): Party {
    return serviceHub.identityService.wellKnownPartyFromX500Name(x509)!!
}

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

fun FlowLogic<Any>.getClaimFrom(serial: String, issuer: String): StateAndRef<IndyClaim> {

    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
    val issuer = QueryCriteria.VaultCustomQueryCriteria(ClaimSchemaV1.PersistentClaim::issuerDid.equal(issuer))
    val serial = QueryCriteria.VaultCustomQueryCriteria(ClaimSchemaV1.PersistentClaim::id.equal(serial))

    val criteria = generalCriteria.and(issuer).and(serial)

    val results = serviceHub.vaultService.queryBy<IndyClaim>(criteria)
    return results.states.singleOrNull()!!
}

fun FlowLogic<Any>.getClaimProof(serial: String): StateAndRef<IndyClaimProof> {

    val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
    val serial = QueryCriteria.VaultCustomQueryCriteria(ClaimProofSchemaV1.PersistentProof::id.equal(serial))

    val criteria = generalCriteria.and(serial)

    val results = serviceHub.vaultService.queryBy<IndyClaimProof>(criteria)
    return results.states.singleOrNull()!!
}