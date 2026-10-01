package com.langoa.app.domain.repository

import com.langoa.app.domain.model.BuildingDefinition
import com.langoa.app.domain.model.Civilization
import kotlinx.coroutines.flow.Flow

interface CivilizationRepository {
    fun getCivilization(languageCode: String): Flow<Civilization?>
    suspend fun refreshCivilization(languageCode: String): Result<Civilization>
    suspend fun getBuildingDefinitions(languageCode: String): Result<List<BuildingDefinition>>
    suspend fun buildBuilding(languageCode: String, buildingType: String, positionX: Int, positionY: Int): Result<Civilization>
    suspend fun upgradeBuilding(languageCode: String, buildingId: String): Result<Civilization>
    suspend fun moveBuilding(languageCode: String, buildingId: String, positionX: Int, positionY: Int, rotationDeg: Int): Result<Civilization>
}
