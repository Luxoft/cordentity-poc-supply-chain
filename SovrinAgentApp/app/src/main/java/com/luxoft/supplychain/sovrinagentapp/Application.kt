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

package com.luxoft.supplychain.sovrinagentapp

import android.app.Application
import com.luxoft.supplychain.sovrinagentapp.data.*
import com.luxoft.supplychain.sovrinagentapp.di.*
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.startKoin

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(myModule))

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().build())

        Realm.getDefaultInstance().executeTransaction {

            it.where(ClaimAttribute::class.java).findAll().deleteAllFromRealm()
            it.where(Product::class.java).findAll().deleteAllFromRealm()

            val product2 = it.createObject(Product::class.java, "N/A")
            product2.state  = PackageState.NEW.name
            product2.medicineName = "Santorium"
            product2.requestedAt = System.currentTimeMillis()
        }
    }
}
