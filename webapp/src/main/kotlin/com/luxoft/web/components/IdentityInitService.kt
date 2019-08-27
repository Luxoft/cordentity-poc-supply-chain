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

package com.luxoft.web.components

import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateCredentialDefinitionFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateRevocationRegistryFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.CreateSchemaFlow
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.ConfigHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.models.RevocationRegistryInfo
import com.luxoft.blockchainlab.hyperledger.indy.models.SchemaId
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.poc.supplychain.data.schema.IndySchema
import com.luxoft.poc.supplychain.flow.IndyUtilsFlow
import com.luxoft.web.components.flow.IndyFlows
import com.luxoft.web.components.flow.IndyService
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import org.hyperledger.indy.sdk.did.Did
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.time.Duration
import java.util.*
import kotlin.reflect.full.declaredMemberFunctions

interface IdentityInitService {
    fun issueIndyMeta(schema: IndySchema, timeout: Duration): Pair<SchemaId, RevocationRegistryInfo>?
}

@Service
@Profile("corda")
class CordaIdentityInitService(private val rpc: RPCComponent): IdentityInitService {
    override fun issueIndyMeta(schema: IndySchema, timeout: Duration): Pair<SchemaId, RevocationRegistryInfo> {
        rpc.services.startFlow(IndyUtilsFlow::GrantTrust).returnValue.get()

        val schemaId = rpc.services.startFlow(
            CreateSchemaFlow::Authority, schema.schemaName, schema.schemaVersion, schema.attributes
        ).returnValue.getOrThrow(timeout).getSchemaIdObject()

        val credDefId =
            rpc.services.startFlow(CreateCredentialDefinitionFlow::Authority, schemaId, true).returnValue.getOrThrow(timeout)
                .getCredentialDefinitionIdObject()

        val revocationRegistry =
            rpc.services.startFlow(CreateRevocationRegistryFlow::Authority, credDefId, 100).returnValue.getOrThrow(timeout)

        return Pair(schemaId, revocationRegistry)
    }
}

@Service
@Profile("mock")
class MockIdentityInitService(val indyFlowExecutor: IndyFlows, val indy: IndyService) : IdentityInitService {
    fun grantTrust() {
        val nym = indy.indyUser.ledgerUser.getNym(indy.indyUser.walletUser.getIdentityDetails())
        if (nym.result.getData() != null) return

        val genesisFile = File(ConfigHelper.getGenesisPath())
        val pool = PoolHelper.openOrCreate(genesisFile, "TrusteePool${Math.abs(Random().nextInt())}")

        val TRUSTEE_SEED = "000000000000000000000000Trustee1"
        val trusteeWalletName = "Trustee"
        val trusteeWalletPassword = "123"

        WalletHelper.createOrTrunc(trusteeWalletName, trusteeWalletPassword)
        val trusteeWallet = WalletHelper.openExisting(trusteeWalletName, trusteeWalletPassword)
        val trusteeDid = Did.createAndStoreMyDid(trusteeWallet, """{"seed":"$TRUSTEE_SEED"}""").get()

        IndyPoolLedgerUser(pool, trusteeDid.did) {
            IndySDKWalletUser(trusteeWallet, trusteeDid.did).sign(it)
        }.storeNym(indy.indyUser.walletUser.getIdentityDetails().copy(role = "TRUSTEE"))
        trusteeWallet.close()
        pool.close()
    }

    override fun issueIndyMeta(schema: IndySchema, timeout: Duration): Pair<SchemaId, RevocationRegistryInfo>? {
        if (indyFlowExecutor.issuer.getRevocationRegistryLike(schema.schemaName) != null)
            return null

        grantTrust()
        val schemaId = indyFlowExecutor.issuer.createSchemaFlow(schema.schemaName, schema.schemaVersion, schema.attributes).getSchemaIdObject()

        val credDefId = indyFlowExecutor.issuer.createCredentialDefinitionFlow(schemaId, true).getCredentialDefinitionIdObject()

        val revReg = indyFlowExecutor.issuer.createRevocationRegistryFlow(credDefId, 100)

        return Pair(schemaId, revReg)
    }
}
