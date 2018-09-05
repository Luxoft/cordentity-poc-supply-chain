package com.luxoft.poc.supplychain.data.schema

abstract class IndySchema(val schemaName: String, val schemaVersion: String) {
    abstract fun getSchemaAttrs(): List<IndySchemaBuilder.AttrTypes>
    override fun toString(): String = "${schemaName}:${schemaVersion}"
}