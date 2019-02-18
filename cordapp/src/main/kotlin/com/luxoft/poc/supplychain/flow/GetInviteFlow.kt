package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.b2c.connectionService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.concurrent.CompletableFuture


class GetInviteFlow {

    @InitiatingFlow
    @StartableByRPC
    open class Treatment : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            CompletableFuture.runAsync { connectionService().getConnection().waitForCounterParty() }

            return connectionService().getConnection().genInvite().invite
        }
    }
}