package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.io.File
import java.nio.file.Paths


class GetTailsFlow {

    @InitiatingFlow
    @StartableByRPC
    open class Treatment : FlowLogic<Map<String, String>>() {

        @Suspendable
        override fun call(): Map<String, String> {
            val tailsPath = indyUser().walletUser.getTailsPath()
            return File(tailsPath).list().associate { it to Paths.get(tailsPath, it).toFile().readText() }
        }
    }
}