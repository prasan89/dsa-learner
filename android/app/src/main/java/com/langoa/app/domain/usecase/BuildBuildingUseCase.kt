package com.langoa.app.domain.usecase

import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.repository.CivilizationRepository
import javax.inject.Inject

class BuildBuildingUseCase @Inject constructor(
    private val civilizationRepository: CivilizationRepository
) {
    suspend operator fun invoke(
        languageCode: String,
        buildingType: String,
        positionX: Int = 0,
        positionY: Int = 0
    ): Result<Civilization> {
        return civilizationRepository.buildBuilding(languageCode, buildingType, positionX, positionY)
    }
}
