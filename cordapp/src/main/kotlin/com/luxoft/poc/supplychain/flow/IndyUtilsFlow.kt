package com.luxoft.poc.supplychain.flow

import co.paralleluniverse.fibers.Suspendable
import com.luxoft.blockchainlab.corda.hyperledger.indy.flow.indyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.ConfigHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import org.hyperledger.indy.sdk.did.Did
import java.io.File
import java.util.*


class IndyUtilsFlow {

    @InitiatingFlow
    @StartableByRPC
    open class GrantTrust : FlowLogic<Unit>() {

        @Suspendable
        override fun call() {
            val nym = indyUser().ledgerUser.getNym(indyUser().walletUser.getIdentityDetails())
            nym.result.getData() ?: run {
                grantTrust(indyUser())
            }
        }

        fun grantTrust(to: SsiUser) {
            val genesisFile = File(ConfigHelper.getGenesisPath())
            val pool = PoolHelper.openOrCreate(genesisFile, "TrusteePool${Math.abs(Random().nextInt())}")

            val TRUSTEE_SEED = "000000000000000000000000Trustee1"
            val trusteeWalletName = "Trustee"
            val trusteeWalletPassword = "123"

            WalletHelper.createOrTrunc(trusteeWalletName, trusteeWalletPassword)
            val trusteeWallet = WalletHelper.openExisting(trusteeWalletName, trusteeWalletPassword)
            val trusteeDid = Did.createAndStoreMyDid(trusteeWallet, """{"seed":"$TRUSTEE_SEED"}""").get()

            IndyPoolLedgerUser(pool, trusteeDid.did) {
                IndySDKWalletUser(trusteeWallet, trusteeDid.did).sign(it)
            }.storeNym(to.walletUser.getIdentityDetails().copy(role = "TRUSTEE"))
            trusteeWallet.close()
            pool.close()
        }
    }
}
