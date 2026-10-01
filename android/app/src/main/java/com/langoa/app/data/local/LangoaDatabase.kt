package com.langoa.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.langoa.app.data.local.dao.CachedCivilizationDao
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.entity.CachedCivilization
import com.langoa.app.data.local.entity.CachedLesson

@Database(
    entities = [CachedLesson::class, CachedCivilization::class],
    version = 1,
    exportSchema = false
)
abstract class LangoaDatabase : RoomDatabase() {
    abstract fun cachedLessonDao(): CachedLessonDao
    abstract fun cachedCivilizationDao(): CachedCivilizationDao

    companion object {
        const val DATABASE_NAME = "langoa_db"
    }
}
