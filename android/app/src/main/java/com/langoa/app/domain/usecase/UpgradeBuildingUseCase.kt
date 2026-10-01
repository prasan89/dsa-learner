package com.langoa.app.domain.usecase

import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.repository.CivilizationRepository
import javax.inject.Inject

class UpgradeBuildingUseCase @Inject constructor(
    private val civilizationRepository: CivilizationRepository
) {
    suspend operator fun invoke(languageCode: String, buildingId: String): Result<Civilization> {
        return civilizationRepository.upgradeBuilding(languageCode, buildingId)
    }
}
