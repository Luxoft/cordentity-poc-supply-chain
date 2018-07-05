package com.luxoft.poc.supplychain.data

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class PackageState {
    NEW,
    ISSUED,
    PROCESSED,
    DELIVERED,
    QP_PASSED,
    COLLECTED
}