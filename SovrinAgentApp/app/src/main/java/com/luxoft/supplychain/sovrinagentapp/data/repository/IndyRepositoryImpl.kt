package com.luxoft.supplychain.sovrinagentapp.data.repository

import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.supplychain.sovrinagentapp.data.idatasource.LocalDataSource
import com.luxoft.supplychain.sovrinagentapp.data.idatasource.RemoteDataSource
import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import io.reactivex.Observable
import io.reactivex.Single

class IndyRepositoryImpl constructor(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) : IndyRepository {

    override fun getCredentials(url: String): Single<String> =
            remoteDataSource.getCredentials(url)

    override fun sendProofOnRequest(url: String): Single<String> =
            remoteDataSource.sendProofOnRequest(url)

    override fun receiveProofRequest(url: String): Single<ProofRequest> =
            remoteDataSource.receiveProofRequest(url)

    override fun sendProof(proofRequest: ProofRequest): Single<String> =
            remoteDataSource.sendProof(proofRequest)

}