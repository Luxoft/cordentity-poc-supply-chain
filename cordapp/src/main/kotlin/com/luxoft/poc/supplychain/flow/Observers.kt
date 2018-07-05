package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.node.StatesToRecord

object Observers {

    @InitiatedBy(BroadcastToObservers::class)
    class Observer(val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val flow = ReceiveTransactionFlow(
                    otherSideSession = flowSession,
                    checkSufficientSignatures = true,
                    statesToRecord = StatesToRecord.ALL_VISIBLE
            )
            subFlow(flow)
        }
    }
}