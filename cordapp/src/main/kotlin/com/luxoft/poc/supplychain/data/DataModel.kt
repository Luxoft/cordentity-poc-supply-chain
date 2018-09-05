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
    Insuranse
}

@CordaSerializable
data class ChainOfAuthority(val chain: MutableMap<BusinessEntity, CordaX500Name> = mutableMapOf()) {
    fun add(type: BusinessEntity, name: CordaX500Name): ChainOfAuthority {
        chain[type] = name
        return this
    }
}
