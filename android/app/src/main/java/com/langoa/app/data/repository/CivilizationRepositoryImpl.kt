package com.langoa.app.data.repository

import com.langoa.app.data.local.dao.CachedCivilizationDao
import com.langoa.app.data.local.entity.CachedCivilization
import com.langoa.app.data.remote.api.CivilizationApi
import com.langoa.app.data.remote.model.BuildBuildingRequest
import com.langoa.app.data.remote.model.CivilizationStateResponse
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.repository.CivilizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CivilizationRepositoryImpl @Inject constructor(
    private val civilizationApi: CivilizationApi,
    private val cachedCivilizationDao: CachedCivilizationDao
) : CivilizationRepository {

    override fun getCivilization(languageCode: String): Flow<Civilization?> {
        return cachedCivilizationDao.getCivilizationByLanguage(languageCode).map { it?.toDomain() }
    }

    override suspend fun refreshCivilization(languageCode: String): Result<Civilization> {
        return try {
            val response = civilizationApi.getCivilization(languageCode)
            cachedCivilizationDao.insertCivilization(response.toEntity())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buildBuilding(languageCode: String, buildingType: String): Result<Civilization> {
        return try {
            val response = civilizationApi.buildBuilding(
                languageCode,
                BuildBuildingRequest(languageCode, buildingType)
            )
            cachedCivilizationDao.insertCivilization(response.toEntity())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvailableBuildings(languageCode: String): Result<List<Building>> {
        return try {
            val response = civilizationApi.getAvailableBuildings(languageCode)
            val buildings = response.availableBuildings.map { dto ->
                Building(
                    id = dto.id,
                    buildingType = dto.buildingType,
                    name = dto.name,
                    level = dto.level,
                    isUnlocked = dto.isUnlocked,
                    coinCost = dto.coinCost,
                    foodCost = dto.foodCost,
                    materialsCost = dto.materialsCost,
                    civPowerGrant = dto.civPowerGrant,
                    description = dto.description,
                    xpRequirement = dto.xpRequirement
                )
            }
            Result.success(buildings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CivilizationStateResponse.toEntity() = CachedCivilization(
        id = id,
        userId = userId,
        languageCode = languageCode,
        name = name,
        totalXp = totalXp,
        level = level,
        coins = coins,
        food = food,
        materials = materials,
        civPower = civPower,
        population = population
    )

    private fun CivilizationStateResponse.toDomain() = Civilization(
        id = id,
        userId = userId,
        languageCode = languageCode,
        name = name,
        totalXp = totalXp,
        level = level,
        coins = coins,
        food = food,
        materials = materials,
        civPower = civPower,
        population = population,
        buildings = buildings.map { dto ->
            Building(
                id = dto.id,
                buildingType = dto.buildingType,
                name = dto.name,
                level = dto.level,
                isUnlocked = dto.isUnlocked,
                coinCost = dto.coinCost,
                foodCost = dto.foodCost,
                materialsCost = dto.materialsCost,
                civPowerGrant = dto.civPowerGrant,
                description = dto.description,
                xpRequirement = dto.xpRequirement
            )
        }
    )

    private fun CachedCivilization.toDomain() = Civilization(
        id = id,
        userId = userId,
        languageCode = languageCode,
        name = name,
        totalXp = totalXp,
        level = level,
        coins = coins,
        food = food,
        materials = materials,
        civPower = civPower,
        population = population
    )
}
