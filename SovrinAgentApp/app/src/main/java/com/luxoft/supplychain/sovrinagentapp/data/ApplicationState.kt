package com.luxoft.supplychain.sovrinagentapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.Credential
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofInfo
import java.net.InetAddress
import java.net.URI

class ApplicationState(
        val context: Context,
        val indyPoolIp: InetAddress,
        val indyPoolGenesisPath: URI,
        val indyPoolGenesisContent: (nodeIp: InetAddress) -> String = ::StandardIndyPoolGenesis)
{
    val indy: LiveData<IndyState> = TODO()

    val userCredentials: LiveData<List<Credential>> = TODO()

    val user: LiveData<UserState> = TODO()

    val authenticationHistory: LiveData<List<ProofInfo>> = TODO()
}