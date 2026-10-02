package com.langoa.app.domain.repository

import com.langoa.app.data.remote.model.CoinCreditResponse
import com.langoa.app.data.remote.model.CoinPackageDto

interface CoinPurchaseRepository {
    suspend fun getPackages(): Result<List<CoinPackageDto>>
    suspend fun verifyPlayPurchase(
        packageCode: String,
        purchaseToken: String,
        orderId: String,
        languageCode: String
    ): Result<CoinCreditResponse>
}
