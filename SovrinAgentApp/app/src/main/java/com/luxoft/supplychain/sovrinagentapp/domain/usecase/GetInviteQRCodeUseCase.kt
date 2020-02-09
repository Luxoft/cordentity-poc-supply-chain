package com.luxoft.supplychain.sovrinagentapp.domain.usecase

import android.graphics.Bitmap
import com.luxoft.supplychain.sovrinagentapp.domain.irepository.IndyRepository
import io.reactivex.Observable
import io.reactivex.Single

class GetInviteQRCodeUseCase constructor(
        private val indyRepository: IndyRepository
) {

    fun get(): Single<Bitmap> =
            indyRepository.getInviteQRCode()
}