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

import com.luxoft.web.data.PackagesResponse
import com.luxoft.web.data.ProcessPackageRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

enum class ManufactureEndpoint(val url: String) {
    LIST("/api/mf/package/list"),
    PROCESS("/api/mf/request/process"),
}

class ManufactureClient(private val restTemplate: RestTemplate) {
    fun getPackages(): PackagesResponse = this.restTemplate.getForObject(ManufactureEndpoint.LIST.url)
        ?: throw RuntimeException("Failed to request packages")

    fun processPackage(serial: String) {
        val processRequest = ProcessPackageRequest(serial)

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(processRequest, headers)

        val processResponse = this.restTemplate.postForEntity(ManufactureEndpoint.PROCESS.url, entity, String::class.java)

        if (processResponse.statusCode != HttpStatus.OK) {
            throw RuntimeException("Server threw error: ${processResponse.body}")
        }
    }
}
