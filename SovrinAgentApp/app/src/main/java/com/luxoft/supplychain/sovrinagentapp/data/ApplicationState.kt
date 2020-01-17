package com.luxoft.supplychain.sovrinagentapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.utils.map
import com.luxoft.supplychain.sovrinagentapp.utils.mapNotNull
import java.net.InetAddress
import java.net.URI

class ApplicationState(
        val context: Context,
        indyPoolIp: InetAddress,
        indyPoolGenesisPath: URI,
        indyPoolGenesisContent: (nodeIp: InetAddress) -> String = ::StandardIndyPoolGenesis)
{
    val indyState: IndyState = IndyState(indyPoolIp, indyPoolGenesisPath, indyPoolGenesisContent)

    val walletCredentials: LiveData<List<CredentialReference>> =
        indyState.indyUser
            .map { it.walletUser.getCredentials().asSequence().toList() }

    val user: LiveData<UserState> =
        walletCredentials.mapNotNull { creds ->
            val credRef = creds.firstOrNull { it.getSchemaIdObject().name == "Patient Demographics" }
            val name = credRef?.attributes?.getOrDefault("Full_legal_name", null)?.toString()
            val pic = context.getDrawable(R.drawable.user)

            if(name != null && pic != null)
                UserState(name, pic)
            else
                null
        }

    private val mutAuthenticationHistory = MutableLiveData<List<ProofInfo>>()
    val authenticationHistory: LiveData<List<ProofInfo>> = mutAuthenticationHistory
    init {
        mutAuthenticationHistory.value = listOf()
    }
}