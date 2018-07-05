package com.luxoft.poc.supplychain.data

import net.corda.core.contracts.StateRef
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
sealed class ProcessResult {
    class Success: ProcessResult()
    class Failure(val state: StateRef, val qualityCheck: AcceptanceResult): ProcessResult()
}