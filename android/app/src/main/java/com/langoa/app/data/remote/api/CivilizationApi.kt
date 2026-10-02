package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.*
import retrofit2.http.*

interface CivilizationApi {
    @GET("api/v1/civilization/{language}")
    suspend fun getCivilization(@Path("language") language: String): CivilizationStateResponse

    @GET("api/v1/civilization/{language}/buildings/definitions")
    suspend fun getBuildingDefinitions(@Path("language") language: String): List<BuildingDefinitionDto>

    @POST("api/v1/civilization/{language}/buildings")
    suspend fun buildBuilding(
        @Path("language") language: String,
        @Body request: BuildBuildingRequest
    ): CivilizationStateResponse

    @POST("api/v1/civilization/{language}/buildings/{id}/upgrade")
    suspend fun upgradeBuilding(
        @Path("language") language: String,
        @Path("id") buildingId: String
    ): CivilizationStateResponse

    @PUT("api/v1/civilization/{language}/buildings/{id}/move")
    suspend fun moveBuilding(
        @Path("language") language: String,
        @Path("id") buildingId: String,
        @Body request: MoveBuildingRequest
    ): CivilizationStateResponse

    @GET("api/v1/civilization/{language}/quests")
    suspend fun getQuests(@Path("language") language: String): List<QuestDto>

    @GET("api/v1/civilization/{language}/achievements")
    suspend fun getAchievements(@Path("language") language: String): List<AchievementDto>

    @POST("api/v1/civilization/{language}/resources/collect")
    suspend fun collectResources(@Path("language") language: String): CivilizationStateResponse
}
