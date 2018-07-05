package com.luxoft.web.data

val SUCCESS = mapOf("success" to true)
val FAILURE = mapOf("success" to false)


data class Serial(val serial: String)

data class QPReleaseResult(val serial: String, val status: String) // 'success' | 'fail';

data class TreatmentCenterDetails(val name: String)

data class PushToken(val did: String, val token: String)

data class AskForPackageRequest(val tcName: String)

data class ProcessPackageRequest(val serial: String)

data class PackagesResponse(val packages: List<Package>)

data class ErrorResponse(val type: String?, val message: String?)

data class Package(
        val serial: String = "",
        val status: Int = 0,
        val manufacturer: String = "",
        val patientDid: String = "",
        val patientDiagnosis: String = "",
        val medicineName: String = "",
        val medicineDescription: String = "",
        val treatmentCenterName: String = "",
        val treatmentCenterAddress: String = "",
        val issuedAt: Long = 0L,
        val issuedBy: String = "",
        val processedAt: Long = 0L,
        val processedBy: String = "",
        val deliveredAt: Long = 0L,
        val deliveredBy: String = "",
        val collectedAt: Long = 0L,
        val collectedBy: String = "",
        val qp: Boolean = false
)