package com.luxoft.supplychain.sovrinagentapp.di

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luxoft.supplychain.sovrinagentapp.communcations.SovrinAgentService
import io.realm.RealmObject
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import retrofit.RxJavaCallAdapterFactory


// Koin module
val myModule : Module = applicationContext {
    bean { provideGson() }
    bean { provideApiClient(get()) } // get() will resolve Service instance
}


fun provideApiClient(gson: Gson): SovrinAgentService {

    val retrofit: Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("http://10.0.2.2:8083")
            .build()

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