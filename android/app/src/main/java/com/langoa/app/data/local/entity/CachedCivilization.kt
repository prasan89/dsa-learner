package com.langoa.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_civilizations")
data class CachedCivilization(
    @PrimaryKey val id: String,
    val userId: String,
    val languageCode: String,
    val name: String,
    val totalXp: Int,
    val level: Int,
    val coins: Int,
    val food: Int,
    val materials: Int,
    val civPower: Int,
    val population: Int,
    val buildingsJson: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)
