package com.langoa.app.data.repository

import com.langoa.app.data.remote.api.CoinPurchaseApi
import com.langoa.app.data.remote.model.CoinCreditResponse
import com.langoa.app.data.remote.model.CoinPackageDto
import com.langoa.app.data.remote.model.VerifyPlayCoinRequest
import com.langoa.app.domain.repository.CoinPurchaseRepository
import javax.inject.Inject

class CoinPurchaseRepositoryImpl @Inject constructor(
    private val coinPurchaseApi: CoinPurchaseApi
) : CoinPurchaseRepository {

    override suspend fun getPackages(): Result<List<CoinPackageDto>> = runCatching {
        coinPurchaseApi.getPackages()
    }

    override suspend fun verifyPlayPurchase(
        packageCode: String,
        purchaseToken: String,
        orderId: String,
        languageCode: String
    ): Result<CoinCreditResponse> = runCatching {
        coinPurchaseApi.verifyPlay(
            VerifyPlayCoinRequest(packageCode, purchaseToken, orderId, languageCode)
        )
    }
}
