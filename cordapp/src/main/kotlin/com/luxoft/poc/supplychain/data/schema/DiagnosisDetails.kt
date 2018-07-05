package com.luxoft.poc.supplychain.data.schema

class DiagnosisDetails: IndySchemaBuilder() {

    object Attributes {
        object Stage : AttrTypes by Attribute("stage")
        object Disease : AttrTypes by Attribute("disease")
        object MedicineName : AttrTypes by Attribute("medicineName")
        object Recommendation : AttrTypes by Attribute("recommendation")
    }

    companion object {

        val schemaName = "medicine_diagnosis"
        val schemaVersion = "1.0"

        val schemaKey = "{ \"name\":\"${schemaName}\",\"version\":\"${schemaVersion}\",\"did\":\"%s\"}"
    }

    override fun getSchemaAttrs(): List<AttrTypes> = listOf(
            Attributes.Stage,
            Attributes.Disease,
            Attributes.MedicineName,
            Attributes.Recommendation
    )
}