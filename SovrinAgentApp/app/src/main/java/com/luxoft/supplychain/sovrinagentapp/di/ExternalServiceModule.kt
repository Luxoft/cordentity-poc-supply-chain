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
import com.luxoft.supplychain.sovrinagentapp.application.BASE_URL
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import io.realm.RealmObject
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory
import java.util.concurrent.TimeUnit

// Koin module
val ExternalServiceModule: Module = module {
    single<Gson> {
        val exclusionStrategy = object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean = f.declaringClass == RealmObject::class.java
            override fun shouldSkipClass(clazz: Class<*>): Boolean = false
        }

        GsonBuilder().setExclusionStrategies(exclusionStrategy).create()
    }

    single<SovrinAgentService> {
        val retrofit: Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .baseUrl(BASE_URL)
            .build()

        retrofit.client().setReadTimeout(1, TimeUnit.MINUTES)
        retrofit.create(SovrinAgentService::class.java)
    }
}
