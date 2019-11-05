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

package com.luxoft.poc.supplychain.service

import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.ConfigHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.hyperledger.indy.sdk.did.Did
import java.io.File
import java.lang.Math.abs
import java.util.*

@CordaService
class IndyUtilsService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {
    //TODO: add this logic to cordentity
    fun grantTrust(to: SsiUser) {
        val genesisFile = File(ConfigHelper.getGenesisPath())
        val pool = PoolHelper.openOrCreate(genesisFile, "TrusteePool${abs(Random().nextInt())}")

        val TRUSTEE_SEED = "000000000000000000000000Trustee1"
        val trusteeWalletName = "Trustee"
        val trusteeWalletPassword = "123"

        WalletHelper.createOrTrunc(trusteeWalletName, trusteeWalletPassword)
        val trusteeWallet = WalletHelper.openExisting(trusteeWalletName, trusteeWalletPassword)
        val trusteeDid = Did.createAndStoreMyDid(trusteeWallet, """{"seed":"$TRUSTEE_SEED"}""").get()

        IndyPoolLedgerUser(pool, trusteeDid.did) {
            IndySDKWalletUser(trusteeWallet, trusteeDid.did).sign(it)
        }.storeNym(to.walletUser.getIdentityDetails().copy(role = "TRUSTEE"))
        trusteeWallet.close()
        pool.close()
    }
}

fun FlowLogic<Any>.indyUtils() = serviceHub.cordaService(IndyUtilsService::class.java)
