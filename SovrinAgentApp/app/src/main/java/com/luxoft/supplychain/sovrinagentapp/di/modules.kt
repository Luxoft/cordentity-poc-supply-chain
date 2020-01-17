package com.luxoft.supplychain.sovrinagentapp.di

import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_IP
import com.luxoft.supplychain.sovrinagentapp.application.GENESIS_PATH
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import java.net.InetAddress
import java.net.URI

val applicationsStateModule = module {
    single<ApplicationState> {
        ApplicationState(
            androidContext(),
            InetAddress.getByName(GENESIS_IP),
            URI.create(GENESIS_PATH)
        )
    }
}