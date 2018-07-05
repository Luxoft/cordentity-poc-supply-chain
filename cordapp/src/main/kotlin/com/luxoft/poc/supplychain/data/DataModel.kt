package com.luxoft.poc.supplychain.data

import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable


@CordaSerializable
data class PackageInfo (
        val serial: String,
        val state: PackageState,

        val patientDid: String,
        val patientAgent: CordaX500Name,
        val patientDiagnosis: String?,

        val medicineName: String?,
        val medicineDescription: String?,

        val requestedAt: Long? = null,
        val requestedBy: CordaX500Name,

        val issuedAt: Long? = null,
        val issuedBy: CordaX500Name? = null,

        val processedAt: Long? = null,
        val processedBy: CordaX500Name? = null,

        val deliveredAt: Long? = null,
        val deliveredTo: CordaX500Name? = null,

        val qp: Boolean? = null,

        val collectedAt: Long? = null
)

@CordaSerializable
data class AcceptanceResult (
        val serial: String,
        val isAccepted: Boolean = true,
        val comments: String? = null
)

@CordaSerializable
enum class BusinessEntity {
    Treatment,
    Manufacturer,
    Goverment,
    Insuranse,
}

@CordaSerializable
data class ChainOfAuthority(val chain: MutableMap<BusinessEntity, CordaX500Name> = mutableMapOf()) {
    fun add(type: BusinessEntity, name: CordaX500Name): ChainOfAuthority {
        chain[type] = name
        return this
    }
}