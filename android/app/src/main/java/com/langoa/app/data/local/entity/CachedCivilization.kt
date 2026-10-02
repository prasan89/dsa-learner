package com.langoa.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_civilizations")
data class CachedCivilization(
    @PrimaryKey val id: String,
    val languageCode: String,
    val name: String,
    val civilizationTier: String,
    val tierLevel: Int,
    val totalXp: Long,
    val totalLessonsCompleted: Int,
    val balancesJson: String = "{}",
    val buildingsJson: String = "[]",
    val decorationsJson: String = "[]",
    val unlockedExpansionSlotsJson: String = "[]",
    @ColumnInfo(defaultValue = "{}") val capacitiesJson: String = "{}",
    val cachedAt: Long = System.currentTimeMillis()
)
