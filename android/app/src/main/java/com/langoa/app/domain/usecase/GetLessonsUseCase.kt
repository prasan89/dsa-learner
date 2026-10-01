package com.langoa.app.domain.usecase

import com.langoa.app.domain.model.Lesson
import com.langoa.app.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    operator fun invoke(languageCode: String): Flow<List<Lesson>> {
        return learningRepository.getLessonsForLanguage(languageCode)
    }
}
