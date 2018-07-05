package com.luxoft.poc.supplychain.data.schema

class PersonalInformation: IndySchemaBuilder() {

    object Attributes {
        object Nationality : IndySchemaBuilder.AttrTypes by Attribute("nationality")
        object Forename : IndySchemaBuilder.AttrTypes by Attribute("forename")
        object Age : IndySchemaBuilder.AttrTypes by Attribute("age")
    }

    companion object {

        val schemaName = "personal_information"
        val schemaVersion = "1.0"

        val schemaKey = "{ \"name\":\"${schemaName}\",\"version\":\"${schemaVersion}\",\"did\":\"%s\"}"
    }

    override fun getSchemaAttrs(): List<IndySchemaBuilder.AttrTypes> = listOf(
        Attributes.Age,
        Attributes.Nationality,
        Attributes.Forename
    )
}