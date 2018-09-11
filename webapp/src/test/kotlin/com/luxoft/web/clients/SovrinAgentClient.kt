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

package com.luxoft.web.clients

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.luxoft.web.data.AskForPackageRequest
import com.luxoft.web.data.ErrorResponse
import com.luxoft.web.data.Package
import com.luxoft.web.data.Serial
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType


enum class SovrinAgentEndpoint(val url: String) {
    LIST("/api/sa/package/list"),
    INIT("/api/sa/request/create"),
    COLLECT("/api/sa/package/receive")
}


class SovrinAgentClient(private val restTemplate: TestRestTemplate) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun getPackages(): List<Package> {
        val packagesResponseJson = this.restTemplate.getForObject(SovrinAgentEndpoint.LIST.url, String::class.java)

        try {
            return mapper.readValue(packagesResponseJson)

        } catch (e: JsonParseException) {
            val errorResponse = mapper.readValue<ErrorResponse>(packagesResponseJson)
            throw RuntimeException("Sovrin agent is unable to get packages. Remote server threw error: $errorResponse")
        }
    }

    fun initFlow(tcName: String) {
        val initRequest = AskForPackageRequest(tcName)
        val initRequestJson = mapper.writeValueAsString(initRequest)

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(initRequestJson, headers)

        val initResponse = this.restTemplate.postForEntity(SovrinAgentEndpoint.INIT.url, entity, String::class.java)

        if (initResponse == null || initResponse.statusCode != OK) {
            val errorResponse = mapper.readValue<ErrorResponse>(initResponse.body)
            throw RuntimeException("Sovrin agent is unable to init flow with TC $tcName. Remote server threw error: $errorResponse")
        }
    }

    fun collectPackage(serial: String, tcName: String) {
        val collectRequest = Serial(serial)
        val collectRequestJson = mapper.writeValueAsString(collectRequest)
        val collectResponse = this.restTemplate.postForObject(SovrinAgentEndpoint.COLLECT.url, collectRequestJson, String::class.java)

        if (collectResponse != "Ok") {
            val errorResponse = mapper.readValue<ErrorResponse>(collectResponse)
            throw RuntimeException("Sovrin agent is unable to collect package $serial. Remote server threw error: $errorResponse")
        }
    }
}
