package com.luxoft.poc.supplychain.data.schema

import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.AttrTypes
import com.luxoft.poc.supplychain.data.schema.IndySchemaBuilder.Attribute

object PackageReceipt: IndySchema(schemaName = "package_receipt", schemaVersion = "1.0") {
    object Attributes {
        object Serial : AttrTypes by Attribute("serial")
    }

    override fun getSchemaAttrs(): List<AttrTypes> = listOf(Attributes.Serial)
}