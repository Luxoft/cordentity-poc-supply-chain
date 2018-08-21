package com.luxoft.poc.supplychain.data.schema

import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.AttrTypes
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.Attribute

object DiagnosisDetails: IndySchema(schemaName = "medicine_diagnosis", schemaVersion = "1.0") {

    object Attributes {
        object Stage : AttrTypes by Attribute("stage")
        object Disease : AttrTypes by Attribute("disease")
        object MedicineName : AttrTypes by Attribute("medicineName")
        object Recommendation : AttrTypes by Attribute("recommendation")
    }

    override fun getSchemaAttrs(): List<AttrTypes> = listOf(
            Attributes.Stage,
            Attributes.Disease,
            Attributes.MedicineName,
            Attributes.Recommendation
    )
}