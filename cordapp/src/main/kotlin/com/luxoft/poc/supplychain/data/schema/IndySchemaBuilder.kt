package com.luxoft.poc.supplychain.data.schema

class IndySchemaBuilder {

    interface AttrTypes {
        val name: String
    }
    data class Attribute(override val name: String) : AttrTypes

    private val builder: StringBuilder = StringBuilder()

    fun addAttr(type: AttrTypes, attr: String): IndySchemaBuilder {
        if(!builder.isEmpty()) builder.append(",")

        builder.append(String.format("\"%s\":[\"%s\",\"%s\"]" , type.name, attr, "22"))
        return this
    }

    fun build(): String = String.format("{%s}", builder.toString())
}