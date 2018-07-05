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