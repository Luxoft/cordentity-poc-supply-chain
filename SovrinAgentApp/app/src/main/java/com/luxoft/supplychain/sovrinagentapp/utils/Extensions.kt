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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.luxoft.blockchainlab.hyperledger.indy.wallet.WalletUser
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import io.realm.Realm

fun Context.inflate(@LayoutRes res: Int, parent: ViewGroup? = null): View {
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
    val claims = this.getCredentials().asSequence().asIterable()
            .flatMap { credRef ->
                val schema = credRef.getSchemaIdObject()
                val credDefId = credRef.getCredentialDefinitionIdObject()

                credRef.attributes.map { attribute ->
                    ClaimAttribute().apply {
                        key = attribute.key
                        value = attribute.value?.toString()
                        schemaName = schema.name
                        schemaVersion = schema.version
                        issuerDid = credDefId.did
                        credRefSeqNo = credDefId.schemaSeqNo
                    }
                }
            }

    Realm.getDefaultInstance().executeTransaction { realm ->
        // todo: Update only updated claims instead of full realm.delete
        realm.delete(ClaimAttribute::class.java)
        realm.copyToRealmOrUpdate(claims)
    }
}