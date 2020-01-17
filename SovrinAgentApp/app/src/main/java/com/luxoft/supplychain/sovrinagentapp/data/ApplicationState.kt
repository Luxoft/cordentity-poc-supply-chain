package com.luxoft.supplychain.sovrinagentapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.utils.mapNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.URI

class ApplicationState(
        val context: Context,
        indyPoolIp: InetAddress,
        indyPoolGenesisPath: URI,
        indyPoolGenesisContent: (nodeIp: InetAddress) -> String = ::StandardIndyPoolGenesis)
{
    val indyState: IndyState = IndyState(indyPoolIp, indyPoolGenesisPath, indyPoolGenesisContent)

    private val medWalletCredentials = MediatorLiveData<List<CredentialReference>>()
    val walletCredentials: LiveData<List<CredentialReference>> = medWalletCredentials
    init {
        medWalletCredentials.addSource(indyState.indyUser) {}
    }

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

    fun updateWalletCredentials() {
        val credList = indyState.indyUser.value?.walletUser?.getCredentials()?.asSequence()?.toList()
        GlobalScope.launch(Dispatchers.Main) {
            if(credList != null  && credList != medWalletCredentials.value)
                medWalletCredentials.value = credList
        }
    }
}