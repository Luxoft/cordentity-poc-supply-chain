package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC


class GetInviteFlow {

    @InitiatingFlow
    @StartableByRPC
    open class Treatment : FlowLogic<AgentConnection.ReceiveInviteMessage>() {

        @Suspendable
        override fun call(): AgentConnection.ReceiveInviteMessage {
            return serviceHub.cordaService(ConnectionService::class.java).invite
        }
    }
}