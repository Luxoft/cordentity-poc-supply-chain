package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.corda.hyperledger.indy.handle
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.awaitFiber
import com.luxoft.blockchainlab.corda.hyperledger.indy.service.connectionService
import com.luxoft.blockchainlab.hyperledger.indy.helpers.TailsHelper
import com.luxoft.poc.supplychain.service.clientResolverService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class GetInviteFlow {

    companion object {
        val inviteWaitTimeout = Pair<Long, TimeUnit>(10, TimeUnit.SECONDS)
    }

    @InitiatingFlow
    @StartableByRPC
    open class Treatment(val clientId: UUID) : FlowLogic<String>() {

        @Suspendable
        override fun call(): String {
            val invite = connectionService().getConnection().generateInvite().awaitFiber()
            clientResolverService().userUuid2Did[clientId] = CompletableFuture()
            CompletableFuture.runAsync {
                connectionService().getConnection().waitForInvitedParty(invite, 300000)
                        .handle { message, ex ->
                            if (ex != null) {
                                logger.error("Failed to wait for invited party", ex)
                                return@handle
                            }
                            clientResolverService().userUuid2Did[clientId]!!.complete(message!!.partyDID())
                            message.handleTailsRequestsWith {
                                TailsHelper.DefaultReader(indyUser().walletUser.getTailsPath()).read(it)
                            }
                        }
            }.exceptionally { logger.error("Error in invite future", it); null; }

            return invite
        }
    }
}

fun CompletableFuture<String>.get(time: Pair<Long, TimeUnit>) = get(time.first, time.second)