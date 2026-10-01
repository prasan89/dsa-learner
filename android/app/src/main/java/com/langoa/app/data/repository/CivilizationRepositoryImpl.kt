package com.langoa.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.langoa.app.data.local.dao.CachedCivilizationDao
import com.langoa.app.data.local.entity.CachedCivilization
import com.langoa.app.data.remote.api.CivilizationApi
import com.langoa.app.data.remote.model.BuildBuildingRequest
import com.langoa.app.data.remote.model.CivilizationStateResponse
import com.langoa.app.data.remote.model.MoveBuildingRequest
import com.langoa.app.domain.model.Building
import com.langoa.app.domain.model.BuildingDefinition
import com.langoa.app.domain.model.BuildingLevelConfig
import com.langoa.app.domain.model.Civilization
import com.langoa.app.domain.model.Decoration
import com.langoa.app.domain.repository.CivilizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CivilizationRepositoryImpl @Inject constructor(
    private val civilizationApi: CivilizationApi,
    private val cachedCivilizationDao: CachedCivilizationDao,
    private val gson: Gson
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

    override suspend fun getBuildingDefinitions(languageCode: String): Result<List<BuildingDefinition>> {
        return try {
            val dtos = civilizationApi.getBuildingDefinitions(languageCode)
            val definitions = dtos.map { dto ->
                BuildingDefinition(
                    buildingType = dto.buildingType,
                    displayName = dto.displayName,
                    description = dto.description,
                    maxLevel = dto.maxLevel,
                    isUnlocked = dto.isUnlocked,
                    canBuild = dto.canBuild,
                    currentLevel = dto.currentLevel,
                    levels = dto.levels.map { lvl ->
                        BuildingLevelConfig(
                            level = lvl.level,
                            coinCost = lvl.coinCost,
                            foodCost = lvl.foodCost,
                            materialCost = lvl.materialCost,
                            woodCost = lvl.woodCost,
                            requiredLessonsCompleted = lvl.requiredLessonsCompleted,
                            requiredXp = lvl.requiredXp
                        )
                    }
                )
            }
            Result.success(definitions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun buildBuilding(
        languageCode: String,
        buildingType: String,
        positionX: Int,
        positionY: Int
    ): Result<Civilization> {
        return try {
            val response = civilizationApi.buildBuilding(
                languageCode,
                BuildBuildingRequest(buildingType, positionX, positionY)
            )
            cachedCivilizationDao.insertCivilization(response.toEntity())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun upgradeBuilding(languageCode: String, buildingId: String): Result<Civilization> {
        return try {
            val response = civilizationApi.upgradeBuilding(languageCode, buildingId)
            cachedCivilizationDao.insertCivilization(response.toEntity())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveBuilding(
        languageCode: String,
        buildingId: String,
        positionX: Int,
        positionY: Int,
        rotationDeg: Int
    ): Result<Civilization> {
        return try {
            val response = civilizationApi.moveBuilding(
                languageCode,
                buildingId,
                MoveBuildingRequest(positionX, positionY, rotationDeg)
            )
            cachedCivilizationDao.insertCivilization(response.toEntity())
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CivilizationStateResponse.toEntity() = CachedCivilization(
        id = id,
        languageCode = languageCode,
        name = name,
        civilizationTier = civilizationTier,
        tierLevel = tierLevel,
        totalXp = totalXp,
        totalLessonsCompleted = totalLessonsCompleted,
        balancesJson = gson.toJson(balances),
        buildingsJson = gson.toJson(buildings),
        decorationsJson = gson.toJson(decorations),
        unlockedExpansionSlotsJson = gson.toJson(unlockedExpansionSlots)
    )

    private fun CivilizationStateResponse.toDomain() = Civilization(
        id = id,
        languageCode = languageCode,
        name = name,
        civilizationTier = civilizationTier,
        tierLevel = tierLevel,
        totalXp = totalXp,
        totalLessonsCompleted = totalLessonsCompleted,
        balances = balances,
        buildings = buildings.map { dto ->
            Building(
                id = dto.id,
                buildingType = dto.buildingType,
                displayName = dto.displayName,
                level = dto.level,
                positionX = dto.positionX,
                positionY = dto.positionY,
                buildState = dto.buildState,
                rotationDeg = dto.rotationDeg,
                widthTiles = dto.widthTiles,
                heightTiles = dto.heightTiles
            )
        },
        decorations = decorations.map { dto ->
            Decoration(
                id = dto.id,
                decorationType = dto.decorationType,
                displayName = dto.displayName,
                positionX = dto.positionX,
                positionY = dto.positionY,
                rotationDeg = dto.rotationDeg
            )
        },
        unlockedExpansionSlots = unlockedExpansionSlots
    )

    private fun CachedCivilization.toDomain(): Civilization {
        val balancesType = object : TypeToken<Map<String, Long>>() {}.type
        val balances: Map<String, Long> = gson.fromJson(balancesJson, balancesType) ?: emptyMap()
        return Civilization(
            id = id,
            languageCode = languageCode,
            name = name,
            civilizationTier = civilizationTier,
            tierLevel = tierLevel,
            totalXp = totalXp,
            totalLessonsCompleted = totalLessonsCompleted,
            balances = balances
        )
    }
}
