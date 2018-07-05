package com.luxoft.poc.supplychain.data


import net.corda.core.serialization.SerializationWhitelist

class Whitelist : SerializationWhitelist {

    override val whitelist: List<Class<*>>
        get() = listOf(
        )
}