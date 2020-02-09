package com.luxoft.supplychain.sovrinagentapp.data.idatasource

import android.graphics.Bitmap
import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyPartyConnection
import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import io.reactivex.Observable
import io.reactivex.Single


interface LocalDataSource {
}

interface RemoteDataSource {
    fun getCredentials(url : String): Single<String>
    fun sendProofOnRequest(url: String): Single<String>
    fun receiveProofRequest(url: String): Single<ProofRequest>
    fun sendProof(proofRequest: ProofRequest): Single<String>
    fun getInviteQRCode(): Single<Bitmap>
}