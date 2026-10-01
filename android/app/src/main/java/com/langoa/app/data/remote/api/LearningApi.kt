package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.CurriculumDto
import com.langoa.app.data.remote.model.LessonCompletionRequest
import com.langoa.app.data.remote.model.LessonCompletionResponse
import com.langoa.app.data.remote.model.LessonDetailDto
import com.langoa.app.data.remote.model.LessonOverviewDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LearningApi {
    @GET("api/v1/academy/{language}/curriculum")
    suspend fun getCurriculum(@Path("language") language: String): CurriculumDto

    @GET("api/v1/academy/{language}/lessons/{lessonId}")
    suspend fun getLesson(
        @Path("language") language: String,
        @Path("lessonId") lessonId: String
    ): LessonDetailDto

    @GET("api/v1/academy/{language}/lessons/{lessonId}/overview")
    suspend fun getLessonOverview(
        @Path("language") language: String,
        @Path("lessonId") lessonId: String
    ): LessonOverviewDto

    @POST("api/v1/academy/{language}/lessons/{lessonId}/complete")
    suspend fun completeLesson(
        @Path("language") language: String,
        @Path("lessonId") lessonId: String,
        @Body request: LessonCompletionRequest
    ): LessonCompletionResponse
}
