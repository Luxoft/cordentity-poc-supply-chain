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

package com.luxoft.supplychain.sovrinagentapp.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.blockchainlab.hyperledger.indy.DEFAULT_MASTER_SECRET_ID
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.WalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.getOwnDids
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.ui.GENESIS_PATH
import io.realm.Realm
import io.realm.RealmObject
import org.hyperledger.indy.sdk.pool.Pool
import org.hyperledger.indy.sdk.wallet.Wallet
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import rx.Single
import java.io.File
import java.util.concurrent.TimeUnit


// Koin module
val myModule: Module = module {
    single { provideGson() }
    single { provideApiClient(get()) } // get() will resolve Service instance
    factory { provideWalletAndPool() }
    single { provideIndyUser(get()) }
    single { connectedAgentConnection() }
}

val webServerEndpoint = "http://3.17.65.252:8082"
val indyAgentWSEndpoint = "ws://3.17.65.252:8094/ws"
val tailsPath = "/sdcard/tails"

//Async agent initialization for smooth UX

val agentConnection = PythonRefAgentConnection()
val agentConnect = agentConnection.connect(indyAgentWSEndpoint, login = "medical-supplychain", password = "secretPassword").toCompletable()
fun connectedAgentConnection(): AgentConnection {
    agentConnect.await()
    return agentConnection
}

//Async indy initialization for smooth UX
lateinit var pool: Pool
lateinit var wallet: Wallet

val indyInit = Single.create<Unit> { observer ->
    try {
        pool = PoolHelper.openOrCreate(File(GENESIS_PATH), "pool")
        wallet = WalletHelper.openOrCreate("medical-supplychain", "password")
        observer.onSuccess(Unit)
    } catch (e: Exception) {
        observer.onError(RuntimeException("Error initializing indy", e))
    }
}

val indyInitialize by lazy {
    indyInit.toCompletable()
}

fun provideWalletAndPool(): Pair<Wallet, Pool> {
    indyInitialize.await()

    return Pair(wallet, pool)
}

fun provideIndyUser(walletAndPool: Pair<Wallet, Pool>): IndyUser {
    val (wallet, pool) = walletAndPool
    val walletUser = wallet.getOwnDids().firstOrNull()?.run {
        IndySDKWalletUser(wallet, did, tailsPath)
    } ?: run {
        IndySDKWalletUser(wallet, tailsPath = tailsPath).apply { createMasterSecret(DEFAULT_MASTER_SECRET_ID) }
    }

    return IndyUser(walletUser, IndyPoolLedgerUser(pool, walletUser.did, walletUser::sign), false)
}

fun WalletUser.updateCredentialsInRealm() {
    Realm.getDefaultInstance().executeTransaction {
        val claims = this.getCredentials().asSequence().map { credRef ->
            credRef.attributes.entries.map {
                ClaimAttribute().apply {
                    key = it.key
                    value = it.value?.toString()
                    schemaId = credRef.schemaIdRaw
                }
            }
        }.flatten().toList()
        it.delete(ClaimAttribute::class.java)
        it.copyToRealmOrUpdate(claims)
    }
}

fun provideApiClient(gson: Gson): SovrinAgentService {
    val retrofit: Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(webServerEndpoint)
            .build()

    retrofit.client().setReadTimeout(1, TimeUnit.MINUTES)

    return retrofit.create(SovrinAgentService::class.java)
}

fun provideGson(): Gson {
    return GsonBuilder().setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            return f.declaringClass == RealmObject::class.java
        }

        override fun shouldSkipClass(clazz: Class<*>): Boolean {
            return false
        }
    }).create()
}
