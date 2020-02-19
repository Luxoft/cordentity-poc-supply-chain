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

class ApplicationState(
        val context: Context,
        val indyState: IndyState)
{
    private val refreshedIndyUser = VolatileLiveDataHolder(indyState.indyUser)
    val walletCredentials: LiveData<List<CredentialReference>> = refreshedIndyUser.liveData.map { indyUser ->
        indyUser.walletUser.getCredentials().asSequence().toList()
    }

    val user: LiveData<UserState> =
        walletCredentials.map { creds ->
            val credRef = creds.firstOrNull { it.getSchemaIdObject().name == KnownSchemas.PersonalId.schemaName }
            val name = credRef?.attributes?.getOrDefault(KnownSchemas.PersonalId.attributes.name, null)?.toString()
            val pic = context.getDrawable(R.drawable.user)

            UserState(name, pic)
        }

    private val mutAuthenticationHistory = MutableLiveData(initialValue = listOf<VerificationEvent>())
    val authenticationHistory: LiveData<List<VerificationEvent>> = mutAuthenticationHistory

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

