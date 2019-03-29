package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.handle
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.awaitFiber
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.connectionService
import com.luxoft.poc.supplychain.service.clientResolverService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.*
import java.util.concurrent.CompletableFuture


class GetInviteFlow {

    @InitiatingFlow
    @StartableByRPC
    open class Treatment(val clientId: UUID) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            val invite = connectionService().getConnection().generateInvite().awaitFiber()
            CompletableFuture.runAsync {
                connectionService().getConnection().waitForInvitedParty(invite)
                        .handle { message, ex ->
                            if (ex != null) {
                                logger.error("Failed to wait for invited party", ex)
                                return@handle
                            }
                            clientResolverService().userUuid2Did[clientId] = message!!.partyDID()
                        }
            }.exceptionally { logger.error("Error in invite future", it); null; }

            return invite
        }
    }
}