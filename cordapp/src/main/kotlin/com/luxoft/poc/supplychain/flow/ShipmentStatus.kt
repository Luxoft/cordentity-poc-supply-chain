package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.poc.supplychain.data.ProcessResult
import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.state.getObservers
import com.luxoft.poc.supplychain.except
import com.luxoft.poc.supplychain.runSessions
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap


object ShipmentStatus {

    @InitiatingFlow
    @StartableByRPC
    class Notifier(private val acceptanceCheck: AcceptanceResult) : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val packageRef = getPackageState(acceptanceCheck.serial, ourIdentity)

            val flowToObservers = packageRef.getObservers()
                    .except(ourIdentity)
                    .runSessions(this)

            if(acceptanceCheck.isAccepted) {
                flowToObservers.forEach { it.send(ProcessResult.Success()) }
            } else flowToObservers.forEach {
                val shipmentRef = getShipmentState(acceptanceCheck.serial, true)
                it.send(ProcessResult.Failure(shipmentRef.ref, acceptanceCheck))
            }
        }
    }

    @InitiatedBy(Notifier::class)
    class Observer(private val flowSession: FlowSession): FlowLogic<Unit>() {

        @Suspendable
        fun handleFailure(shipment: StateRef, qualityCheck: AcceptanceResult) {
            // TODO: package wasnt accepted - need to handle it
        }

        @Suspendable
        override fun call() {
            flowSession.receive<ProcessResult>().unwrap { processResult ->
                when (processResult) {
                    is ProcessResult.Failure -> handleFailure(processResult.state, processResult.qualityCheck)
                    is ProcessResult.Success -> Unit
                }
            }
        }
    }
}