package com.langoa.app.domain.usecase

import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.repository.CivilizationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCivilizationUseCase @Inject constructor(
    private val civilizationRepository: CivilizationRepository
) {
    operator fun invoke(languageCode: String): Flow<Civilization?> {
        return civilizationRepository.getCivilization(languageCode)
    }

    suspend fun refresh(languageCode: String): Result<Civilization> {
        return civilizationRepository.refreshCivilization(languageCode)
    }
}
