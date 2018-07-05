package com.luxoft.poc.supplychain.data.schema

import java.security.SecureRandom

abstract class IndySchemaBuilder {

    interface AttrTypes {
        val name: String
    }
    data class Attribute(override val name: String) : AttrTypes

    private val builder: StringBuilder = StringBuilder()

    val rnd = SecureRandom()

    fun addAttr(type: AttrTypes, attr: String): IndySchemaBuilder {
        if(!builder.isEmpty()) builder.append(",")

        builder.append(String.format("\"%s\":[\"%s\",\"%s\"]" , type.name, attr, "22"))
        return this
    }

    fun build(): String = String.format("{%s}", builder.toString())

    abstract fun getSchemaAttrs(): List<AttrTypes>
}