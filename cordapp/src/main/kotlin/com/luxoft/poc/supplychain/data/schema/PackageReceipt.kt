package com.luxoft.poc.supplychain.data.schema

class PackageReceipt: IndySchemaBuilder() {

    object Attributes {
        object Serial : IndySchemaBuilder.AttrTypes by Attribute("serial")
    }

    companion object {

        val schemaName = "package_receipt"
        val schemaVersion = "1.0"

        val schemaKey = "{ \"name\":\"${schemaName}\",\"version\":\"${schemaVersion}\",\"did\":\"%s\"}"
    }

    override fun getSchemaAttrs(): List<IndySchemaBuilder.AttrTypes> = listOf(Attributes.Serial)
}