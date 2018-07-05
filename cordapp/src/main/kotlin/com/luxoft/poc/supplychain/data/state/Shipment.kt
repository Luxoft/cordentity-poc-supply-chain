package com.luxoft.poc.supplychain.data.state

import com.luxoft.poc.supplychain.data.AcceptanceResult
import com.luxoft.poc.supplychain.data.schema.ShipmentSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class Shipment(val serial: String,
                    val from: AbstractParty,
                    val to: AbstractParty,
                    val shipmentCheck: AcceptanceResult? = null) : LinearState, QueryableState {

    override val linearId: UniqueIdentifier = UniqueIdentifier()
    override val participants: List<AbstractParty> = listOf(from, to)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ShipmentSchemaV1 -> ShipmentSchemaV1.PersistentShipment(
                    serial = serial,
                    isAccepted = shipmentCheck?.isAccepted,
                    comments = shipmentCheck?.comments
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ShipmentSchemaV1)
}

fun StateAndRef<Shipment>.getParties() = this.state.data.participants