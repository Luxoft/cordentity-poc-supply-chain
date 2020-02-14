package com.luxoft.supplychain.sovrinagentapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.luxoft.blockchainlab.hyperledger.indy.models.CredentialReference
import com.luxoft.supplychain.sovrinagentapp.R
import com.luxoft.supplychain.sovrinagentapp.utils.MutableLiveData
import com.luxoft.supplychain.sovrinagentapp.utils.VolatileLiveDataHolder
import com.luxoft.supplychain.sovrinagentapp.utils.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.URI

class ApplicationState(
//        val context: Context,
        indyPoolIp: InetAddress,
        indyPoolGenesisPath: URI,
        indyPoolTailsPath: URI,
        indyPoolGenesisContent: (nodeIp: InetAddress) -> String = ::StandardIndyPoolGenesis)
{
    val indyState: IndyState = IndyState(
            indyPoolIp,
            indyPoolGenesisPath,
            indyPoolTailsPath,
            indyPoolGenesisContent)

    private val refreshedIndyUser = VolatileLiveDataHolder(indyState.indyUser)
    val walletCredentials: LiveData<List<CredentialReference>> = refreshedIndyUser.liveData.map { indyUser ->
        indyUser.walletUser.getCredentials().asSequence().toList()
    }

    lateinit var context: Context
    val user: LiveData<UserState> =
            walletCredentials.map { creds ->
                val credRef = creds.firstOrNull { it.getSchemaIdObject().name == KnownSchemas.PersonalId.schemaName }
//                val name = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.name, null)?.toString()
//                val pic = context.getDrawable(R.drawable.user)
                val firstName = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.firstName, null)?.toString()
                val birthDate = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.birthDate, null)?.toString()
                val photo = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.photo, null)?.toString()
                val secondName = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.secondName, null)?.toString()
                val swissPassNum = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.swissPassNum, null)?.toString()

//                UserState(name,pic)
                UserState(firstName, birthDate, photo, secondName, swissPassNum)
            }

    private val mutAuthenticationHistory = MutableLiveData(initialValue = listOf<VerificationEvent>())
    val authenticationHistory: LiveData<List<VerificationEvent>> = mutAuthenticationHistory

    val credentialPresentationRules = CredentialPresentationRules()
    val credentialAttributePresentationRules = CredentialAttributePresentationRules()

    fun updateWalletCredentials() {
        GlobalScope.launch(Dispatchers.Main) {
            refreshedIndyUser.refresh()
        }
    }

    fun clearLocalData() {
        indyState.resetWallet()
        GlobalScope.launch(Dispatchers.Main) {
            mutAuthenticationHistory.value = listOf()
        }
    }

    fun storeVerificationEvent(event: VerificationEvent) {
        val oldList = mutAuthenticationHistory.value ?: listOf()
        GlobalScope.launch(Dispatchers.Main) {
            mutAuthenticationHistory.value = oldList + listOf(event)
        }
    }
}

