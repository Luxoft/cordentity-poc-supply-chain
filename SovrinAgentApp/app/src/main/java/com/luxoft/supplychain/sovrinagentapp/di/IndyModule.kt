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

import android.app.AlertDialog
import com.luxoft.blockchainlab.hyperledger.indy.DEFAULT_MASTER_SECRET_ID
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.WalletUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.getOwnIdentities
import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_PATH
import com.luxoft.supplychain.sovrinagentapp.application.TAILS_PATH
import com.luxoft.supplychain.sovrinagentapp.ui.activities.splashScreen
import org.hyperledger.indy.sdk.pool.Pool
import org.hyperledger.indy.sdk.wallet.Wallet
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import rx.Completable
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

// Koin module
val IndyModule: Module = module {

    val (w, p) = provideWalletAndPool()

    factory<Wallet> {
        w
    }

    factory<Pool> {
        p
    }

    single<IndyUser> {
        val wallet = get<Wallet>()
        val existingDid = wallet.getOwnIdentities().firstOrNull()?.did

        val walletUser: WalletUser
        if(existingDid != null) {
            walletUser = IndySDKWalletUser(wallet, existingDid, TAILS_PATH)
        } else {
            walletUser = IndySDKWalletUser(wallet, tailsPath = TAILS_PATH)
            walletUser.createMasterSecret(DEFAULT_MASTER_SECRET_ID)
        }

        val ledgerUser = IndyPoolLedgerUser(get<Pool>(), walletUser.did, walletUser::sign)
        IndyUser(walletUser, ledgerUser, createDefaultMasterSecret = false)
    }
}

//Async indy initialization for smooth UX
lateinit var pool: Pool
lateinit var wallet: Wallet

fun indyInitialize() : Boolean {
    var t: Thread? = null
    val observable = Observable.create<Unit> { observer ->
        try {
            t = Thread {
                pool = PoolHelper.openOrCreate(File(GENESIS_PATH), "pool")
                wallet = WalletHelper.openOrCreate("medical-supplychain", "password")
            }
            t?.apply { run(); join() }
            observer.onNext(Unit)
        } catch (e: Exception) {
            observer.onError(RuntimeException("Error initializing indy", e))
        }
    }
    return try {
        observable
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.newThread())
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .first()
        true
    } catch (e: Exception) {
        t?.interrupt()
        false
    }
}

fun provideWalletAndPool(): Pair<Wallet, Pool> {

    var retry = true
    var dialog: AlertDialog? = null
    while (retry) {
        if (indyInitialize())
            retry = false
        else {
            Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe {
                dialog = AlertDialog.Builder(splashScreen)
                        .setTitle("Indy Pool")
                        .setMessage("Please check Internet connection and tap RETRY")
                        .setCancelable(false)
                        .setPositiveButton("RETRY") { _, _ -> retry = true }
                        .setNegativeButton("EXIT") { _, _ -> splashScreen.finish(); retry = false }
                        .create()
                splashScreen.runOnUiThread {
                    dialog?.show()
                }
            }
            Thread.sleep(1000)
            while (dialog == null || dialog!!.isShowing) { Thread.yield() }
        }
    }
    return Pair(wallet, pool)
}