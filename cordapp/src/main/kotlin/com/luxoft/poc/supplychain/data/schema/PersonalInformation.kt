package com.luxoft.poc.supplychain.data.schema

object PersonalInformation: IndySchema(schemaName = "personal_information", schemaVersion = "1.0") {
    object Attributes {
        object Nationality : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("nationality")
        object Forename : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("forename")
        object Age : IndySchemaBuilder.AttrTypes by IndySchemaBuilder.Attribute("age")
    }

    override fun getSchemaAttrs(): List<IndySchemaBuilder.AttrTypes> = listOf(
        Attributes.Age,
        Attributes.Nationality,
        Attributes.Forename
    )
}