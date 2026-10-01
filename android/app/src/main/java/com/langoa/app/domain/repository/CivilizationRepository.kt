package com.langoa.app.domain.repository

import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.Civilization
import kotlinx.coroutines.flow.Flow

interface CivilizationRepository {
    fun getCivilization(languageCode: String): Flow<Civilization?>
    suspend fun refreshCivilization(languageCode: String): Result<Civilization>
    suspend fun buildBuilding(languageCode: String, buildingType: String): Result<Civilization>
    suspend fun getAvailableBuildings(languageCode: String): Result<List<Building>>
}
