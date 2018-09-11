/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

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
