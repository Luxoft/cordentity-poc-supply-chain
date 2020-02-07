package com.luxoft.supplychain.sovrinagentapp.domain.usecase

import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import io.reactivex.Observable
import io.reactivex.Single

class GetCredentialsUseCase constructor(
        private val indyRepository: IndyRepository
) {

    fun get(url: String): Single<String> =
            indyRepository.getCredentials(url)
}