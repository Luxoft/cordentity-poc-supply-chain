package com.luxoft.blockchainlab.corda.hyperledger.indy.service

import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyPartyConnection
import com.luxoft.blockchainlab.hyperledger.indy.IndyUser
import com.luxoft.blockchainlab.hyperledger.indy.SsiUser
import com.luxoft.blockchainlab.hyperledger.indy.helpers.GenesisHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.PoolHelper
import com.luxoft.blockchainlab.hyperledger.indy.helpers.WalletHelper
import com.luxoft.blockchainlab.hyperledger.indy.ledger.IndyPoolLedgerUser
import com.luxoft.blockchainlab.hyperledger.indy.models.*
import com.luxoft.blockchainlab.hyperledger.indy.wallet.IndySDKWalletUser
import org.apache.commons.io.FileUtils
import org.hyperledger.indy.sdk.pool.Pool
import rx.Single
import java.io.File
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class IndyPartyConnectionMock : IndyPartyConnection {
    companion object {
        fun ssiTestUser(): SsiUser {
            val pool: Pool
            val poolName = "test-pool-${Math.abs(Random().nextInt())}"
            val tmpTestWalletId = "tmpTestWallet${Math.abs(Random().nextInt())}"

            val genesisFile = File("../devops/profile/develop/genesis/indy_pool_lumedic.txn")
            if (!GenesisHelper.exists(genesisFile))
                throw RuntimeException("Genesis file ${genesisFile.absolutePath} doesn't exist")

            PoolHelper.createOrTrunc(genesisFile, poolName)
            pool = PoolHelper.openExisting(poolName)

            //creating user wallet with credentials required by logic
            File(this.javaClass.classLoader.getResource("testUserWallet.db").file)
                .copyTo(File("${FileUtils.getUserDirectory()}/.indy_client/wallet/$tmpTestWalletId/sqlite.db"))
                .apply {
                    Runtime.getRuntime().addShutdownHook(Thread { FileUtils.deleteQuietly(parentFile) })
                }

            val wallet = WalletHelper.openExisting(tmpTestWalletId, "password")

            val walletUser = IndySDKWalletUser(wallet)
            val ledgerUser = IndyPoolLedgerUser(pool, walletUser.getIdentityDetails().did) { walletUser.sign(it) }
            return IndyUser.with(walletUser).with(ledgerUser).build()
        }

        val indyUser by lazy {
            ssiTestUser()
        }
    }

    override fun myDID(): String {
        return indyUser.walletUser.getIdentityDetails().did
    }

    override fun partyDID(): String {
        return indyUser.walletUser.getIdentityDetails().did + "-party"
    }

    val proofInfo = Event<ProofInfo>()

    override fun receiveProof(): Single<ProofInfo> {
        return proofInfo.receive()
    }

    override fun sendProof(proof: ProofInfo) {
        proofInfo.send(proof)
    }

    override fun sendProofRequest(request: ProofRequest) {
        val proof = indyUser.createProofFromLedgerData(request)
        sendProof(proof)
    }

    override fun handleTailsRequestsWith(handler: (TailsRequest) -> TailsResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveCredential(): Single<CredentialInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveCredentialOffer(): Single<CredentialOffer> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveCredentialRequest(): Single<CredentialRequestInfo> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveProofRequest(): Single<ProofRequest> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestTails(tailsHash: String): Single<TailsResponse> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendCredential(credential: CredentialInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendCredentialOffer(offer: CredentialOffer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendCredentialRequest(request: CredentialRequestInfo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class Event<T> {
    private var queue = LinkedBlockingQueue<T>()

    fun send(event: T) = queue.add(event)

    fun receive(): Single<T> = Single.create { single -> single.onSuccess(queue.take()) }
}
