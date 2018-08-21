package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.GetDidFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.VerifyClaimFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.data.ChainOfAuthority
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.schema.PackageReceipt
import com.luxoft.poc.supplychain.data.schema.PersonalInformation
import com.luxoft.poc.supplychain.data.state.Package
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.data.state.getObservers
import com.luxoft.poc.supplychain.data.state.getParties
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class PackageWithdrawal {

    @InitiatingFlow
    @StartableByRPC
    class Owner(val serial: String) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val packageIn = getPackageState(serial, PackageState.DELIVERED)

            val receiptOwner = packageIn.getInfo().requestedBy
            val receiptOwnerDid = subFlow(GetDidFlow.Initiator(receiptOwner))

            val clientWasAuthenticatedIn = getClaimProof(serial)
            val clientGotPackageReceiptIn = getClaimFrom(serial, receiptOwnerDid)
            //val authCommand = Command(ClaimChecker.Commands.Verify())

            val info = packageIn.getInfo().copy(
                    state = PackageState.COLLECTED,
                    collectedAt = System.currentTimeMillis())

            val collectedPackage = packageIn.state.data.collect(ourIdentity, info)
            val packageOut = StateAndContract(collectedPackage, PackageContract::class.java.name)
            val packageCmd = Command(PackageContract.Collect(), packageIn.getParties().mapToKeys())

            val trxBuilder = TransactionBuilder(whoIsNotary()).withItems(
                    //clientWasAuthenticatedIn,
                    //clientGotPackageReceiptIn,
                    packageIn,
                    packageOut,
                    packageCmd
            )

            val flowSessions = packageIn.getParties()
                    .except(ourIdentity)
                    .runSessions(this)

            val selfSignedTx = serviceHub.signInitialTransaction(trxBuilder, ourIdentity.owningKey)
            val signedTrx = subFlow(CollectSignaturesFlow(selfSignedTx, flowSessions))
            val finalTrx = subFlow(FinalityFlow(signedTrx))

            waitForLedgerCommit(finalTrx.id)
            subFlow(BroadcastToObservers(packageIn.getObservers(), finalTrx))
        }
    }

    @InitiatedBy(Owner::class)
    open class Holder(val flowSession: FlowSession) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    val filter = QueryCriteria.VaultQueryCriteria(stateRefs = stx.inputs)
                    val packageIn = serviceHub.vaultService.queryBy<Package>(filter).states.singleOrNull()

                    // TODO: ask developers about cheque usecase
                    //require(checkReceipt(packageIn!!.getInfo().serial)) { "Owner doesnt have digital receipt on package" }
                }
            }

            subFlow(signTransactionFlow)
        }

        @Suspendable
        private fun checkReceipt(serial: String): Boolean {
            val packageSchemaId = getCacheSchemaId(PackageReceipt)
            val packageCredDefId = getCacheCredDefId(PackageReceipt)

            val attributes = listOf(
                    VerifyClaimFlow.ProofAttribute(packageSchemaId, packageCredDefId, PackageReceipt.Attributes.Serial.name, serial)
            )

            val proverName = flowSession.counterparty.name

            return subFlow(VerifyClaimFlow.Verifier(serial, attributes, emptyList(), proverName))
        }
    }
}