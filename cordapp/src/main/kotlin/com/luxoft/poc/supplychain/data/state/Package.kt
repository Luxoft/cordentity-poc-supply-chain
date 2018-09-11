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

import com.luxoft.poc.supplychain.data.PackageInfo
import com.luxoft.poc.supplychain.data.schema.PackageSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class Package(
        val info: PackageInfo,
        val owner: AbstractParty,
        val observers: List<AbstractParty>,
        override val participants: List<AbstractParty> = listOf(owner),
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState, QueryableState {


    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PackageSchemaV1 -> PackageSchemaV1.PersistentPackage(this)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PackageSchemaV1)

    fun requestForTransfer(destination: AbstractParty, info: PackageInfo): Package =
            copy(owner = destination, info = info, participants = listOf(owner, destination))

    fun ship(destination: AbstractParty, info: PackageInfo): Package =
            copy(info = info, participants = listOf(destination))

    fun collect(owner: AbstractParty, info: PackageInfo): Package =
            copy(info = info, owner = owner)
}

fun StateAndRef<Package>.getInfo() = this.state.data.info
fun StateAndRef<Package>.getParties() = this.state.data.participants
fun StateAndRef<Package>.getObservers() = this.state.data.observers
