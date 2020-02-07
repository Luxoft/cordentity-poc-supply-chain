package com.luxoft.supplychain.sovrinagentapp.domain.usecase

import com.luxoft.blockchainlab.hyperledger.indy.models.ProofRequest
import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import io.reactivex.Observable
import io.reactivex.Single

class GetProofRequestUseCase constructor(
        private val indyRepository: IndyRepository
) {

    fun get(url: String): Single<ProofRequest> =
            indyRepository.receiveProofRequest(url)
}