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

package com.luxoft.lumedic.ssi.corda.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.contextLogger
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


@CordaService
class EpicCommunicationService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    val log = serviceHub.contextLogger()
    val mapper: ObjectMapper = jacksonObjectMapper()
    var client = OkHttpClient()
    private val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")!!
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH).withZone(ZoneOffset.UTC)

    private val epicEndpointUrl = serviceHub.getAppContext().config.getString("EpicEndpointUrl")
    private val epicKey = serviceHub.getAppContext().config.getString("EpicSubscriptionKey")

    private val initialState = SubmitInsurancePost(
        Insurance = SubmitInsurancePost.InsuranceData(
            PayorID = "578",
            GroupNumber = "",
            InsuranceName = "",
            MemberNumber = "931935359",
            SubscriberDateOfBirth = "19750101",
            SubscriberName = "SMITH,JONATHAN"
        )
    )
    private val targetState = SubmitInsurancePost(
        Insurance = SubmitInsurancePost.InsuranceData(
            PayorID = "1344",
            GroupNumber = "",
            InsuranceName = "",
            MemberNumber = "MSJ601067914",
            SubscriberDateOfBirth = "19750101",
            SubscriberName = "SMITH,JONATHAN"
        )
    )

    private fun submitInsurancePost(body: SubmitInsurancePost) {
        val bodyJson = mapper.writeValueAsString(body)
        val request =
            Request.Builder()
                .url(epicEndpointUrl)
                .post(RequestBody.create(JSON, bodyJson))
                .addHeader("Ocp-Apim-Subscription-Key", epicKey)
                .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful()) {
                "submitInsurancePost is not successful".apply {
                    log.error(this)
                    log.warn(response.body().toString())
                    throw RuntimeException(this)
                }
            }
        }
    }

    fun updateClientData(credentialProof: ProofInfo) {
        submitInsurancePost(initialState.run {
            copy(
                Insurance = Insurance.copy(
                    GroupNumber = "19",
                    InsuranceName = credentialProof.getAttributeValue("Payor")!!.raw,
                    SubscriberDateOfBirth = dateFormatter.format(
                        Instant.ofEpochMilli(credentialProof.getAttributeValue("Subscriber_date_of_birth_ms")!!.raw.toLong())
                    ),
                    SubscriberName = credentialProof.getAttributeValue("Subscriber_name")!!.raw
                )
            )
        })
        submitInsurancePost(targetState.run {
            copy(
                Insurance = Insurance.copy(
                    GroupNumber = "14",
                    InsuranceName = credentialProof.getAttributeValue("Payor")!!.raw,
                    SubscriberDateOfBirth = dateFormatter.format(
                        Instant.ofEpochMilli(credentialProof.getAttributeValue("Subscriber_date_of_birth_ms")!!.raw.toLong())
                    ),
                    SubscriberName = credentialProof.getAttributeValue("Subscriber_name")!!.raw
                )
            )
        })
    }

    fun resetClientData() {
        submitInsurancePost(targetState.run {
            copy(
                Insurance = Insurance.copy(
                    GroupNumber = "15"
                )
            )
        })
        submitInsurancePost(initialState.run {
            copy(
                Insurance = Insurance.copy(
                    GroupNumber = "18"
                )
            )
        })
    }
}

fun FlowLogic<Any>.epicCommunicationService() = serviceHub.cordaService(EpicCommunicationService::class.java)

data class SubmitInsurancePost(
    val Insurance: InsuranceData,
    val Patient: PatientData = PatientData()
) {
    data class PatientData(
        val ID: String = "MockPatientID",
        val Type: String = "EXTERNAL"
    )

    data class InsuranceData(
        val GroupNumber: String,
        val InsuranceName: String,
        val MemberNumber: String,
        val PayorID: String = "MockPayorID",
        val PayorIDType: String = "INTERNAL",
        val RelationshipToSubscriber: String = "01",
        val SubscriberDateOfBirth: String,
        val SubscriberName: String
    ) {
        val SubscriberID get() = Date().time.toString()
    }
}
