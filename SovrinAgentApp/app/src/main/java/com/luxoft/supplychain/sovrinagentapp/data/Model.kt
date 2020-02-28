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

enum class PackageState {
    GETPROOFS,
    NEW
}

enum class PopupStatus {
    IN_PROGRESS,
    RECEIVED,
    HISTORY
}

data class Invite(val invite: String, @JsonProperty("clientUUID") val clientUUID: String?)

@RealmClass
open class ClaimAttribute : RealmObject() {
    @PrimaryKey
    open var key: String? = null
    open var value: String? = null
    open var schemaName: String? = null
    open var schemaVersion: String? = null
    open var issuerDid: String? = null
    open var credRefSeqNo: Int = -1
}

@RealmClass
open class Product : RealmObject() {

    fun currentStateTimestamp(state: Int?): Long? {
        return when (state) {
            PackageState.GETPROOFS.ordinal -> 0
            PackageState.NEW.ordinal -> requestedAt
            else -> 0
        }
    }

    fun currentStateMessage(state: Int?): String? {
        return when (state) {
            PackageState.NEW.ordinal -> "Insurer has confirmed the coverage for your prescription"
            else -> this.state ?: ""
        }
    }

    @PrimaryKey
    open var serial: String? = null

    open var state: String? = null
    open var medicineName: String? = null
    open var requestedAt: Long? = null
}

data class AuthorityInfo(
        val did: String,
        val schemaId: String
)

class AuthorityInfoMap : HashMap<String, AuthorityInfo>()