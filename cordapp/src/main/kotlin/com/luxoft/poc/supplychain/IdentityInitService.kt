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

package com.luxoft.poc.supplychain

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateCredentialDefinitionFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateSchemaFlow
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialDefinitionId
import com.luxoft.blockchainlab.hyperledger.indy.models.SchemaId
import com.luxoft.poc.supplychain.data.schema.IndySchema
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import java.time.Duration


class IdentityInitService(private val rpc: CordaRPCOps, private val timeout: Duration = Duration.ofSeconds(30)) {

    fun issueIndyMeta(schema: IndySchema): Pair<SchemaId, CredentialDefinitionId> {
        val schemaId = rpc.startFlow(
            CreateSchemaFlow::Authority, schema.schemaName, schema.schemaVersion, schema.attributes
        ).returnValue.getOrThrow(timeout).getSchemaIdObject()

        val credDefId =
            rpc.startFlow(CreateCredentialDefinitionFlow::Authority, schemaId, false).returnValue.getOrThrow(timeout)
                .getCredentialDefinitionIdObject()

        return Pair(schemaId, credDefId)
    }
}
