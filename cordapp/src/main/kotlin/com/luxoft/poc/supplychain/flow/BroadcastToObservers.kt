package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastToObservers(val observers: List<AbstractParty>,
                           val signedTrx: SignedTransaction) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() = observers.runSessions(this)
            .forEach { subFlow(SendTransactionFlow(it, signedTrx)) }
}