package com.luxoft.supplychain.sovrinagentapp.di

import android.os.Environment
import com.luxoft.blockchainlab.corda.hyperledger.indy.AgentConnection
import com.luxoft.blockchainlab.corda.hyperledger.indy.PythonRefAgentConnection
import com.luxoft.supplychain.sovrinagentapp.application.*
import com.luxoft.supplychain.sovrinagentapp.data.ApplicationState
import com.luxoft.supplychain.sovrinagentapp.data.CredentialAttributePresentationRules
import com.luxoft.supplychain.sovrinagentapp.data.CredentialPresentationRules
import com.luxoft.supplychain.sovrinagentapp.data.IndyState
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import java.net.InetAddress

val applicationsStateModule = module {
    single<IndyState> {
        val phoneStorage = Environment.getExternalStorageDirectory().toURI()

        IndyState(
            InetAddress.getByName(GENESIS_IP),
            phoneStorage.resolve(GENESIS_PATH),
            phoneStorage.resolve(TAILS_PATH)
        )
    }

    single<ApplicationState> {
        ApplicationState(androidContext(), get<IndyState>())
    }

    single<AgentConnection>(createOnStart = true) {
        val agentConnection = PythonRefAgentConnection()
        val agentConnectionProgress = agentConnection.connect(WS_ENDPOINT, WS_LOGIN, WS_PASS)
        agentConnectionProgress.toBlocking().value()  // await
        agentConnection
    }
}

val presenterModule = module {
    single { CredentialPresentationRules() }

    single { CredentialAttributePresentationRules() }
}