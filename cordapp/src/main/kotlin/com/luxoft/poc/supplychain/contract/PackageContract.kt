package com.luxoft.poc.supplychain.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

open class PackageContract : Contract {
    override fun verify(tx: LedgerTransaction) {
    }

    interface Commands : CommandData

    class Request : Commands
    class StartShipment : Commands
    class CompleteShipment : Commands
    class Collect : Commands

}