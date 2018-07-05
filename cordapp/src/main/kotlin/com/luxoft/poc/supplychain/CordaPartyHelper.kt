package com.luxoft.poc.supplychain

import net.corda.core.flows.FlowLogic
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

fun List<AbstractParty>.except(who: AbstractParty) = this.filter { it != who }
fun List<AbstractParty>.except(who: List<AbstractParty>) = this.filter { !who.contains(it) }
fun List<AbstractParty>.mapToKeys() = this.map { it.owningKey }

fun List<AbstractParty>.runSessions(flow: FlowLogic<Any>) = this.map { flow.initiateFlow(it as Party) }