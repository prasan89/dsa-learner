package com.langoa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langoa.app.data.local.entity.CachedCivilization
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedCivilizationDao {
    @Query("SELECT * FROM cached_civilizations WHERE languageCode = :languageCode LIMIT 1")
    fun getCivilizationByLanguage(languageCode: String): Flow<CachedCivilization?>

    @Query("SELECT * FROM cached_civilizations WHERE languageCode = :languageCode LIMIT 1")
    suspend fun getCivilizationByLanguageOnce(languageCode: String): CachedCivilization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCivilization(civilization: CachedCivilization)

    @Query("DELETE FROM cached_civilizations WHERE languageCode = :languageCode")
    suspend fun clearCivilizationForLanguage(languageCode: String)

    @Query("DELETE FROM cached_civilizations")
    suspend fun clearAll()
}
