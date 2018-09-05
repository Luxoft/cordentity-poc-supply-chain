package com.luxoft.web.clients

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.luxoft.web.data.ErrorResponse
import com.luxoft.web.data.Package
import com.luxoft.web.data.PackagesResponse
import com.luxoft.web.data.ProcessPackageRequest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

enum class ManufactureEndpoint(val url: String) {
    LIST("/api/mf/package/list"),
    PROCESS("/api/mf/request/process"),
}

class ManufactureClient(private val restTemplate: TestRestTemplate) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun getPackages(): List<Package> {
        val packagesResponseJson = this.restTemplate.getForObject(ManufactureEndpoint.LIST.url, String::class.java)

        try {
            return mapper.readValue(packagesResponseJson)

        } catch (e: JsonParseException) {
            val errorResponse = mapper.readValue<ErrorResponse>(packagesResponseJson)
            throw RuntimeException("Manufacture is unable to get packages. Remote server threw error: $errorResponse")
        }
    }

    fun processPackage(serial: String) {
        val processRequest = ProcessPackageRequest(serial)
        val processRequestJson = mapper.writeValueAsString(processRequest)

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(processRequestJson, headers)


        val processResponse = this.restTemplate.postForEntity(ManufactureEndpoint.PROCESS.url, entity, String::class.java)

        if (processResponse == null || processResponse.statusCode != HttpStatus.OK) {
            val errorResponse = mapper.readValue<ErrorResponse>(processResponse.body)
            throw RuntimeException("Server threw error: $errorResponse")
        }
    }
}