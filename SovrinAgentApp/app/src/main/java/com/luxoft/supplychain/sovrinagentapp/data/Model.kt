/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.luxoft.supplychain.sovrinagentapp.data


import com.fasterxml.jackson.annotation.JsonProperty
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*
import kotlin.collections.HashMap

enum class PackageState {
    GETPROOFS,
    NEW,
    ISSUED,
    PROCESSED,
    DELIVERED,
    COLLECTED
}

data class Serial(val serial: String, @JsonProperty("clientUUID") val clientUUID: String?)

data class Invite(val invite: String, @JsonProperty("clientUUID") val clientUUID: String?)

data class PushToken(val did: String, val token: String)

data class AskForPackageRequest(val tcName: String, val clientUUID: String)


open class Error {
    open var code: Int = 0
    open var message: String? = null
}

@RealmClass
open class ClaimAttribute : RealmObject() {
    @PrimaryKey
    open var key: String? = null
    open var value: String? = null
    open var schemaId: String? = null
}

@RealmClass
open class Product : RealmObject() {

    fun currentStateTimestamp(state: Int?): Long? {
        return when (state) {
            PackageState.NEW.ordinal -> requestedAt
            PackageState.ISSUED.ordinal -> issuedAt
            PackageState.PROCESSED.ordinal -> processedAt
            PackageState.DELIVERED.ordinal -> deliveredAt
            PackageState.COLLECTED.ordinal -> collectedAt
            else -> 0
        }
    }

    fun currentStateMessage(state: Int?): String? {
        return when (state) {
            PackageState.NEW.ordinal -> "Insurer has confirmed the coverage for your prescription"
            PackageState.ISSUED.ordinal -> "Manufacturing request is created"
            PackageState.PROCESSED.ordinal -> "Medicine is produced"
            PackageState.DELIVERED.ordinal -> "Ready for pick-up"
            PackageState.COLLECTED.ordinal -> "Medicine is collected"
            else -> this.state ?: ""
        }
    }

    @PrimaryKey
    open var serial: String? = null

    open var state: String? = null
    open var description: String? = null

    open var patientDid: String? = null
    open var patientDiagnosis: String? = null

    open var medicineName: String? = null
    open var medicineDescription: String? = null

    open var requestedAt: Long? = null

    open var issuedAt: Long? = null

    open var processedAt: Long? = null

    open var deliveredAt: Long? = null

    open var qp: Boolean? = false

    open var collectedAt: Long? = null
}


@RealmClass
open class Waybill : RealmObject() {
    open var id: String? = null
}

@RealmClass
open class ProductOperation : RealmObject() {
    open var by: String? = null
    @PrimaryKey
    open var at: Long? = null
}

data class AuthorityInfo(
        val did: String,
        val schemaId: String
)

class AuthorityInfoMap : HashMap<String, AuthorityInfo>()