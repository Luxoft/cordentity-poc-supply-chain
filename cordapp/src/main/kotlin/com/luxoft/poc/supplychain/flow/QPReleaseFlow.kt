package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow

object QPReleaseFlow {

    @InitiatingFlow
    class QP(val serial: String) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            return "success"
        }
    }
}