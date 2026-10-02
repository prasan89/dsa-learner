package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.CoinCreditResponse
import com.langoa.app.data.remote.model.CoinPackageDto
import com.langoa.app.data.remote.model.VerifyPlayCoinRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CoinPurchaseApi {

    @GET("api/v1/civilization/coin-packages")
    suspend fun getPackages(): List<CoinPackageDto>

    @POST("api/v1/civilization/coin-packages/verify-play")
    suspend fun verifyPlay(@Body request: VerifyPlayCoinRequest): CoinCreditResponse
}
