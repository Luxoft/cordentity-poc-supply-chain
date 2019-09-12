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

package com.luxoft.supplychain.sovrinagentapp.utils

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.luxoft.blockchainlab.hyperledger.indy.wallet.WalletUser
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import io.realm.Realm

fun Context.inflate(@LayoutRes res: Int, parent: ViewGroup? = null) : View {
    return LayoutInflater.from(this).inflate(res, parent, false)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
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