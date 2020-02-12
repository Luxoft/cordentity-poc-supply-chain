package com.luxoft.lumedic.ssi.corda.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.finalizeTransaction
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.me
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.whoIsNotary
import com.luxoft.lumedic.ssi.corda.contract.ToDoContract
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


class DemoReset {
    @InitiatingFlow
    @StartableByRPC
    open class Hospital : FlowLogic<SecureHash>() {

        companion object {
            object FLOW_INIT : ProgressTracker.Step("Initialising flow.")
            object RESET_EPIC : ProgressTracker.Step("Resetting data in EPIC.")
            object BUILD_CONSUMING_TX : ProgressTracker.Step("Building consuming transaction.")
            object FINALISING_TX : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                FLOW_INIT,
                RESET_EPIC,
                BUILD_CONSUMING_TX,
                FINALISING_TX
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SecureHash {
            progressTracker.currentStep = FLOW_INIT
            val me = me()
            try {
                progressTracker.currentStep = RESET_EPIC

                progressTracker.currentStep = BUILD_CONSUMING_TX
                val states = serviceHub.vaultService.queryBy<ContractState>().states
                val transactionBuilder =
                    TransactionBuilder(whoIsNotary()).addCommand(ToDoContract.Commands.Do(), me.owningKey).apply {
                        states.forEach {
                            addInputState(it)
                        }
                    }
                transactionBuilder.verify(serviceHub)

                progressTracker.currentStep = FINALISING_TX
                val notarizedTx = finalizeTransaction(serviceHub.signInitialTransaction(transactionBuilder))
                waitForLedgerCommit(notarizedTx.id)

                return notarizedTx.id
            } catch (e: Throwable) {
                logger.error("Error in ${this.javaClass.canonicalName}", e)
                throw FlowException(e.message)
            }
        }
    }
}

