package com.luxoft.supplychain.sovrinagentapp.domain.usecase

import android.graphics.Bitmap
import com.luxoft.blockchainlab.corda.hyperledger.indy.IndyPartyConnection
import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import io.reactivex.Observable
import io.reactivex.Single

class WaitForInvitedPartyUseCase constructor(
        private val indyRepository: IndyRepository
) {

    fun get(timeout: Long): Single<IndyPartyConnection> =
            indyRepository.waitForInvitedParty(timeout)
}