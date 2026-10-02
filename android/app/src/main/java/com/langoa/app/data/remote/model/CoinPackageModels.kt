package com.langoa.app.data.remote.model

data class CoinPackageDto(
    val packageCode: String,
    val displayName: String,
    val coinAmount: Long,
    val pricePaise: Int,
    val currency: String,
    val playProductId: String?
)

data class VerifyPlayCoinRequest(
    val packageCode: String,
    val purchaseToken: String,
    val orderId: String,
    val languageCode: String
)

data class CoinCreditResponse(
    val packageCode: String,
    val coinsAwarded: Long,
    val newCoinBalance: Long
)
