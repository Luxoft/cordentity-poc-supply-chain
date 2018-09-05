package com.luxoft.poc.supplychain.data.schema

class IndySchemaBuilder {

    interface AttrTypes {
        val name: String
    }
    data class Attribute(override val name: String) : AttrTypes
    
    private val builder: MutableMap<String, List<String>> =  mutableMapOf()

    fun addAttr(type: AttrTypes, attr: String): IndySchemaBuilder {
        builder[type.name] = listOf(attr, "22")
        return this
    }

    fun build(): String = SerializationUtils.anyToJSON(builder)
}