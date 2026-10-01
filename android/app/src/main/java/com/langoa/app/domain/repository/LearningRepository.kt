package com.langoa.app.domain.repository

import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.model.LessonReward
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    fun getLessonsForLanguage(languageCode: String): Flow<List<Lesson>>
    suspend fun getLessonDetail(languageCode: String, lessonId: String): Result<Lesson>
    suspend fun completeLesson(
        languageCode: String,
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        isPerfect: Boolean
    ): Result<LessonReward>
    suspend fun refreshCurriculum(languageCode: String): Result<Unit>
}
