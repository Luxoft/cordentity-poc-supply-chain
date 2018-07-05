package com.luxoft.poc.supplychain.data.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object ShipmentSchema

object ShipmentSchemaV1 : MappedSchema(
        version = 1,
        schemaFamily = ShipmentSchema.javaClass,
        mappedTypes = listOf(PersistentShipment::class.java)) {

    @Entity
    @Table(name = "shipment")
    class PersistentShipment(
            @Column(name = "serial")
            var serial: String,
            @Column
            var isAccepted: Boolean?,
            @Column
            var comments: String?

    ) : PersistentState() {
        constructor(): this("", null, null)
    }
}