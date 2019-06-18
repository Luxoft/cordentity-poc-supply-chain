package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.poc.supplychain.service.indyUtils
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC


class IndyUtilsFlow {

    @InitiatingFlow
    @StartableByRPC
    open class GrantTrust : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val nym = indyUser().ledgerUser.getNym(indyUser().walletUser.getIdentityDetails())
            nym.result.getData() ?: run {
                indyUtils().grantTrust(indyUser())
            }
        }
    }
}
