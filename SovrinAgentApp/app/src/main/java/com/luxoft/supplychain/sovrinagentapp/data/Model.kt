package com.luxoft.supplychain.sovrinagentapp.data


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

enum class PackageState {
    NEW,
    ISSUED,
    PROCESSED,
    DELIVERED,
    COLLECTED
}

data class Serial(val serial: String)

data class PushToken(val did: String, val token: String)

data class AskForPackageRequest(val tcName: String)



open class Error {
    open var code: Int = 0
    open var message: String? = null
}

@RealmClass
open class ClaimAttribute: RealmObject() {
    @PrimaryKey open var key: String? = null
    open var value: String? = null
    open var issuer: String? = null
}

@RealmClass
open class Product : RealmObject() {

    fun currentStateTimestamp(state: Int?): Long? {
        return when(state) {
            PackageState.NEW.ordinal       -> requestedAt
            PackageState.ISSUED.ordinal    -> issuedAt
            PackageState.PROCESSED.ordinal -> processedAt
            PackageState.DELIVERED.ordinal -> deliveredAt
            PackageState.COLLECTED.ordinal -> collectedAt
            else -> 0
        }
    }

    fun currentStateMessage(state: Int?): String? {
        return when(state) {
            PackageState.NEW.ordinal       -> "Insurer has confirmed the coverage for your prescription"
            PackageState.ISSUED.ordinal    -> "Manufacturing request is created"
            PackageState.PROCESSED.ordinal -> "Medicine is produced"
            PackageState.DELIVERED.ordinal -> "Ready for pick-up"
            PackageState.COLLECTED.ordinal -> "Medicine is collected"
            else -> this.state ?: ""
        }
    }

    @PrimaryKey open var serial: String? = null

    open var state: String? = null
    open var description: String? = null

    open var  patientDid: String? = null
    open var  patientDiagnosis: String? = null

    open var  medicineName: String? = null
    open var  medicineDescription: String? = null

    open var  requestedAt: Long? = null

    open var  issuedAt: Long? = null

    open var  processedAt: Long? = null

    open var  deliveredAt: Long? = null

    open var  qp: Boolean? = false

    open var  collectedAt: Long? = null
}


@RealmClass
open class Waybill : RealmObject() {
    open var id: String? = null
}

@RealmClass
open class ProductOperation : RealmObject() {
    open var by: String? = null
    open var at: Long? = null
}