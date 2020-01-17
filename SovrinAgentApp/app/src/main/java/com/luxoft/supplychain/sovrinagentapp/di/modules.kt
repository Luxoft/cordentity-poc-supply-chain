package com.luxoft.supplychain.sovrinagentapp.di

import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.supplychain.sovrinagentapp.application.*
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

    single<AgentConnection>(createOnStart = true) {
        val agentConnection = PythonRefAgentConnection()
        val agentConnectionProgress = agentConnection.connect(WS_ENDPOINT, WS_LOGIN, WS_PASS)
        agentConnectionProgress.toBlocking().value()  // await
        agentConnection
    }
}