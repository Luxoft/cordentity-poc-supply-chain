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

package com.luxoft.web.data

import com.luxoft.poc.supplychain.data.PackageInfo

val SUCCESS = mapOf("success" to true)
val FAILURE = mapOf("success" to false)


data class Serial(val serial: String, val clientUUID: String? = null)

data class Invite(val invite: String, val clientUUID: String? = null)

data class QPReleaseResult(val serial: String, val status: String) // 'success' | 'fail';

data class TreatmentCenterDetails(val name: String)

data class PushToken(val did: String, val token: String)

data class AskForPackageRequest(val tcName: String, val clientUUID: String)

data class ProcessPackageRequest(val serial: String)

class PackagesResponse : ArrayList<PackageInfo>()

data class ErrorResponse(val type: String?, val message: String?)

data class Package(
        val serial: String = "",
        val status: Int = 0,
        val manufacturer: String = "",
        val patientName: String = "",
        val patientDid: String = "",
        val patientDiagnosis: String = "",
        val insurerDid: String = "",
        val medicineName: String = "",
        val estimatedCost: String = "",
        val isCoveredByInsurer: Boolean = false,
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
