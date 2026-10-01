package com.langoa.app.data.remote.api

import com.langoa.app.data.remote.model.CurriculumDto
import com.langoa.app.data.remote.model.LessonCompletionRequest
import com.langoa.app.data.remote.model.LessonCompletionResponse
import com.langoa.app.data.remote.model.LessonDetailDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LearningApi {
    @GET("api/academy/{languageCode}/curriculum")
    suspend fun getCurriculum(@Path("languageCode") languageCode: String): CurriculumDto

    @GET("api/academy/{languageCode}/lessons/{lessonId}")
    suspend fun getLesson(
        @Path("languageCode") languageCode: String,
        @Path("lessonId") lessonId: String
    ): LessonDetailDto

    @POST("api/academy/{languageCode}/lessons/{lessonId}/complete")
    suspend fun completeLesson(
        @Path("languageCode") languageCode: String,
        @Path("lessonId") lessonId: String,
        @Body request: LessonCompletionRequest
    ): LessonCompletionResponse
}
