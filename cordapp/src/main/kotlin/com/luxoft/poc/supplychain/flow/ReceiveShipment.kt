package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.supplychain.contract.PackageContract
import com.luxoft.poc.supplychain.contract.ShipmentContract
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.PackageState
import com.luxoft.poc.supplychain.data.state.getInfo
import com.luxoft.poc.supplychain.data.state.getObservers
import com.luxoft.poc.supplychain.data.state.getParties
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.mapToKeys
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object ReceiveShipment {

    @InitiatingFlow
    @StartableByRPC
    class Receiver(private val acceptanceCheck: AcceptanceResult) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val packageIn = getPackageState(acceptanceCheck.serial, ourIdentity)
            val shipmentIn = getShipmentState(acceptanceCheck.serial)

            val packageOwners = packageIn.getParties()
            val flowSessionsToPackageOwners = packageOwners
                    .except(ourIdentity)
                    .runSessions(this)

            val dealParticipants = shipmentIn.getParties()
            val flowSessionsToDealParticipants = dealParticipants
                    .except(ourIdentity)
                    .except(packageOwners)
                    .runSessions(this)

            val shipmentCmd =
                    if(acceptanceCheck.isAccepted)
                        Command(ShipmentContract.Accept(), dealParticipants.mapToKeys())
                    else
                        Command(ShipmentContract.Decline(), dealParticipants.mapToKeys())

            val info = packageIn.getInfo().copy(
                    state = PackageState.DELIVERED,
                    deliveredAt = System.currentTimeMillis(),
                    deliveredTo = ourIdentity.name)
            val deliveredPackage = packageIn.state.data.ship(ourIdentity, info)

            val packageOut = StateAndContract(deliveredPackage, PackageContract::class.java.name)
            val packageCmd = Command(PackageContract.CompleteShipment(), packageIn.getParties().mapToKeys())

            val trxBuilder = TransactionBuilder(whoIsNotary()).withItems(
                    packageIn,
                    packageOut,
                    packageCmd,
                    shipmentIn,
                    shipmentCmd
            )

            val flowSessions = flowSessionsToPackageOwners.union(flowSessionsToDealParticipants)

            val selfSignedTx = serviceHub.signInitialTransaction(trxBuilder, ourIdentity.owningKey)
            val signedTrx = subFlow(CollectSignaturesFlow(selfSignedTx, flowSessions))
            val finalTrx = subFlow(FinalityFlow(signedTrx))

            waitForLedgerCommit(finalTrx.id)

            // Notify observers about quality Check status
            //subFlow(ShipmentStatus.Notifier(acceptanceCheck))
            subFlow(BroadcastToObservers(packageIn.getObservers().except(ourIdentity), finalTrx))
        }
    }

    @InitiatedBy(Receiver::class)
    class Counterparty(private val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val signTransactionFlow = object : SignTransactionFlow(flowSession) {
                override fun checkTransaction(stx: SignedTransaction) = Unit
            }
            subFlow(signTransactionFlow)
        }
    }
}