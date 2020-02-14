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

package com.luxoft.supplychain.sovrinagentapp.domain.di

import android.os.Environment
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
import com.luxoft.blockchainlab.hyperledger.indy.wallet.getOwnIdentities
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.SharedPreferencesStore
import com.luxoft.supplychain.sovrinagentapp.data.communcations.SovrinAgentService
import com.luxoft.supplychain.sovrinagentapp.data.idatasource.LocalDataSource
import com.luxoft.supplychain.sovrinagentapp.data.idatasource.RemoteDataSource
import com.luxoft.supplychain.sovrinagentapp.data.repository.IndyRepositoryImpl
import com.luxoft.supplychain.sovrinagentapp.datasource.local.LocalDataSourceImpl
import com.luxoft.supplychain.sovrinagentapp.datasource.remote.RemoteDataSourceImpl
import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import com.luxoft.supplychain.sovrinagentapp.domain.usecase.*
import com.luxoft.supplychain.sovrinagentapp.viewmodel.IndyViewModel
import io.realm.RealmObject
import org.hyperledger.indy.sdk.pool.Pool
import org.hyperledger.indy.sdk.wallet.Wallet
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File
import java.net.InetAddress
import java.net.URI
import java.util.concurrent.TimeUnit

fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
            viewModelModule,
            useCaseModule,
            repositoryModule,
            dataSourceModule,
            sharedPreferencesStoreModule,
            applicationsStateModule,
            agentConnectionModule,
            apiModule
    )
}

val viewModelModule: Module = module {
    viewModel { IndyViewModel(getCredentialsUseCase = get(), getProofRequestUseCase = get(), sendProofUseCase = get(), getInviteQRCodeUseCase = get(), waitForInvitedPartyUseCase = get(),
            sendProofRequestReceiveVerifyUseCase = get()) }
}

val useCaseModule: Module = module {
    factory { GetCredentialsUseCase(indyRepository = get()) }
    factory { GetProofRequestUseCase(indyRepository = get()) }
    factory { SendProofUseCase(indyRepository = get()) }
    factory { GetInviteQRCodeUseCase(indyRepository = get()) }
    factory { WaitForInvitedPartyUseCase(indyRepository = get()) }
    factory { SendProofRequestReceiveVerifyUseCase(indyRepository = get()) }
}

val repositoryModule: Module = module {
    single { IndyRepositoryImpl(localDataSource = get(), remoteDataSource = get()) as IndyRepository }
}

val dataSourceModule: Module = module {
    single { RemoteDataSourceImpl(agentConnection = agentConnection, applicationState = applicationState, sharedPreferencesStore = get()) as RemoteDataSource }
    single { LocalDataSourceImpl() as LocalDataSource }
}

val sharedPreferencesStoreModule: Module = module {
    single { SharedPreferencesStore(androidContext()) }
}


val applicationsStateModule: Module = module {
    single<ApplicationState> {
        applicationState.context = androidContext()
        applicationState
    }
}

val phoneStorage = Environment.getExternalStorageDirectory().toURI()

val applicationState = ApplicationState(
//                androidContext(),
        InetAddress.getByName(GENESIS_IP),
        phoneStorage.resolve(GENESIS_PATH),
        phoneStorage.resolve(TAILS_PATH)
)

val agentConnectionModule: Module = module {
    single<AgentConnection>(createdAtStart = true) {
        agentConnection
    }
}

val agentConnection = PythonRefAgentConnection()
val agentConnectionProgress = agentConnection.connect(WS_ENDPOINT, WS_LOGIN, WS_PASS).toBlocking().value()


//Async indy initialization for smooth UX
//lateinit var pool: Pool
//lateinit var wallet: Wallet
//
//fun indyInitialize() : Boolean {
//    var t: Thread? = null
//    val observable = Observable.create<Unit> { observer ->
//        try {
//            t = Thread {
//                pool = PoolHelper.openOrCreate(File(GENESIS_PATH), "pool")
//                wallet = WalletHelper.openOrCreate("medical-supplychain", "password")
//            }
//            t?.apply { run(); join() }
//            observer.onNext(Unit)
//        } catch (e: Exception) {
//            observer.onError(RuntimeException("Error initializing indy", e))
//        }
//    }
//    return try {
//        observable
//            .observeOn(Schedulers.io())
//            .subscribeOn(Schedulers.newThread())
//            .timeout(10, TimeUnit.SECONDS)
//            .toBlocking()
//            .first()
//        true
//    } catch (e: Exception) {
//        t?.interrupt()
//        false
//    }
//}

//fun provideWalletAndPool(): Pair<Wallet, Pool> {
//
//    var retry = true
//    var dialog: AlertDialog? = null
//    while (retry) {
//        if (indyInitialize())
//            retry = false
//        else {
//            Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe {
//                dialog = AlertDialog.Builder(splashScreen)
//                        .setTitle("Indy Pool")
//                        .setMessage("Please check Internet connection and tap RETRY")
//                        .setCancelable(false)
//                        .setPositiveButton("RETRY") { _, _ -> retry = true }
//                        .setNegativeButton("EXIT") { _, _ -> splashScreen.finish(); retry = false }
//                        .create()
//                splashScreen.runOnUiThread {
//                    dialog?.show()
//                }
//            }
//            Thread.sleep(1000)
//            while (dialog == null || dialog!!.isShowing) { Thread.yield() }
//        }
//    }
//    return Pair(wallet, pool)
//}

//fun provideIndyUser(walletAndPool: Pair<Wallet, Pool>): IndyUser {
//    val (wallet, pool) = walletAndPool
//    val walletUser = wallet.getOwnIdentities().firstOrNull()?.run {
//        IndySDKWalletUser(wallet, did, TAILS_PATH)
//    } ?: run {
//        IndySDKWalletUser(wallet, tailsPath = TAILS_PATH).apply { createMasterSecret(DEFAULT_MASTER_SECRET_ID) }
//    }
//
//    return IndyUser(walletUser, IndyPoolLedgerUser(pool, walletUser.did, walletUser::sign), false)
//}

val apiModule: Module = module {
    single<SovrinAgentService>(createdAtStart = true) {
        api
    }
}


val gson = provideGson()

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

val api:SovrinAgentService = provideApiClient(gson)

fun provideApiClient(gson: Gson): SovrinAgentService {
    val retrofit: Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .build()

    retrofit.client().setReadTimeout(1, TimeUnit.MINUTES)

    return retrofit.create(SovrinAgentService::class.java)
}
