package com.luxoft.poc.supplychain.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

open class ShipmentContract : Contract {


    override fun verify(tx: LedgerTransaction) {

        // if modifiable only by single command
//        val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
            // TODO constraint examples
//            "Should contain exactly 1 input state" using (tx.inputsOfType<StuffState>().size == 1)
//            "Should contain exactly 7 output states" using (tx.outputsOfType<StuffState>().size == 7)
//
//            "Should not contain input cash state" using (tx.inputsOfType<Cash.State>().isEmpty())
//            "Should contain output cash state" using (tx.outputsOfType<Cash.State>().size == 1)
//
//            val cashStates = tx.outputsOfType<Cash.State>()
//
//            cashStates.forEach( {
//                "All of the participants must be signers." using (command.signers.containsAll(it.participants.map { it.owningKey }))
//            })
        }
    }

    interface Commands : CommandData

    class RequestForTransfer : Commands
    class Accept : Commands
    class Decline : Commands
}