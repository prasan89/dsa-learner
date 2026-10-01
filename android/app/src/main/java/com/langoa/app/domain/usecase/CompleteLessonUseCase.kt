package com.langoa.app.domain.usecase

import com.langoa.app.domain.model.LessonReward
import com.langoa.app.domain.repository.LearningRepository
import javax.inject.Inject

class CompleteLessonUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(
        languageCode: String,
        lessonId: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        isPerfect: Boolean
    ): Result<LessonReward> {
        return learningRepository.completeLesson(
            languageCode = languageCode,
            lessonId = lessonId,
            score = score,
            totalQuestions = totalQuestions,
            timeSpentSeconds = timeSpentSeconds,
            isPerfect = isPerfect
        )
    }
}
