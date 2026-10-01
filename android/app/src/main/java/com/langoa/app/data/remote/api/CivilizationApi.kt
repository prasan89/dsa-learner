package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.AvailableBuildingsResponse
import com.langoa.app.data.remote.model.BuildBuildingRequest
import com.langoa.app.data.remote.model.CivilizationStateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CivilizationApi {
    @GET("api/civilization/{languageCode}")
    suspend fun getCivilization(@Path("languageCode") languageCode: String): CivilizationStateResponse

    @GET("api/civilization/{languageCode}/buildings/available")
    suspend fun getAvailableBuildings(@Path("languageCode") languageCode: String): AvailableBuildingsResponse

    @POST("api/civilization/{languageCode}/buildings/build")
    suspend fun buildBuilding(
        @Path("languageCode") languageCode: String,
        @Body request: BuildBuildingRequest
    ): CivilizationStateResponse
}
