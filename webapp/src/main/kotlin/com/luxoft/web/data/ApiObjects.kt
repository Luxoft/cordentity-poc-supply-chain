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

val SUCCESS = mapOf("success" to true)
val FAILURE = mapOf("success" to false)


data class Serial(val serial: String, val clientUUID: String?)

data class Invite(val invite: String, val clientUUID: String)

data class QPReleaseResult(val serial: String, val status: String) // 'success' | 'fail';

data class TreatmentCenterDetails(val name: String)

data class PushToken(val did: String, val token: String)

data class AskForPackageRequest(val tcName: String, val clientUUID: String)

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
