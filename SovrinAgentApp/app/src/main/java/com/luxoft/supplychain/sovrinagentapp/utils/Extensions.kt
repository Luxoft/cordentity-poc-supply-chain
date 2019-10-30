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
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.data.ClaimAttribute
import com.luxoft.supplychain.sovrinagentapp.data.PackageState
import com.luxoft.supplychain.sovrinagentapp.data.Product
import io.realm.Realm
import java.util.*

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
                val credDefIdObject = credRef.getCredentialDefinitionIdObject()

                credRef.attributes.map { attribute ->
                    ClaimAttribute().apply {
                        key = attribute.key
                        value = attribute.value?.toString()
                        schemaName = schema.name
                        schemaVersion = schema.version
                        issuerDid = credDefIdObject.did
                        credDefId = credRef.credentialDefinitionIdRaw
                    }
                }
            }

    Realm.getDefaultInstance().executeTransaction { realm ->
        // todo: Update only updated claims instead of full realm.delete
        realm.delete(ClaimAttribute::class.java)
        realm.copyToRealmOrUpdate(claims)
    }
}

fun addNewProductPrescriptionToRealm(prescriptionAttr: ClaimAttribute) {
    val realm = Realm.getDefaultInstance()

    val diagnosisAttr = realm.where(ClaimAttribute::class.java)
            .equalTo(FIELD_CRED_DEF_ID, prescriptionAttr.credDefId)
            .equalTo(FIELD_KEY, DIAGNOSIS)
            .findFirst()

    val prescribedProduct = Product().apply {
        serial = "package-serial-" + UUID.randomUUID().toString()
        state = PackageState.NEW.name
        medicineName = prescriptionAttr.value
        requestedAt = Long.MAX_VALUE
        patientDiagnosis = diagnosisAttr?.value
    }

    realm.executeTransaction { realm ->
        realm.copyToRealmOrUpdate(prescribedProduct)
    }
}

fun replenishNewProductsInRealm() {
    val realm = Realm.getDefaultInstance()

    val newAndProcessingOrders = realm.where(Product::class.java)
            .isNull(FIELD_COLLECTED_AT)
            .findAll()
            .map { it.medicineName }

    val prescriptions = realm.where(ClaimAttribute::class.java)
            .equalTo(FIELD_KEY, PRESCRIPTION)
            .findAll()

    val spentPrescriptions = prescriptions.filterNot { it.value in newAndProcessingOrders }

    spentPrescriptions.forEach { addNewProductPrescriptionToRealm(it) }
}