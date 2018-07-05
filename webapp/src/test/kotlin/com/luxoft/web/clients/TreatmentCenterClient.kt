package com.luxoft.web.clients

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.luxoft.web.data.ErrorResponse
import com.luxoft.web.data.Package
import com.luxoft.web.data.PackagesResponse
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.stereotype.Component

enum class TreatmentCenterEndpoint(val url: String) {
    LIST("/api/tc/package/list")
}

@Component
class TreatmentCenterClient(private val restTemplate: TestRestTemplate) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun getPackages(): List<Package> {
        val packagesResponseJson = this.restTemplate.getForObject(TreatmentCenterEndpoint.LIST.url, String::class.java)

        try {
            return mapper.readValue<PackagesResponse>(packagesResponseJson).packages

        } catch (e: JsonParseException) {
            val errorResponse = mapper.readValue<ErrorResponse>(packagesResponseJson)
            throw RuntimeException("Treatment center is unable to get packages. Remote server threw error: $errorResponse")
        }
    }
}