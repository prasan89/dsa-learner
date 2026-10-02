package com.langoa.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.langoa.app.data.local.dao.CachedCivilizationDao
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.dao.PendingSyncDao
import com.langoa.app.data.local.entity.CachedCivilization
import com.langoa.app.data.local.entity.CachedLesson
import com.langoa.app.data.local.entity.PendingSync

@Database(
    entities = [CachedLesson::class, CachedCivilization::class, PendingSync::class],
    version = 3,
    exportSchema = false
)
abstract class LangoaDatabase : RoomDatabase() {
    abstract fun cachedLessonDao(): CachedLessonDao
    abstract fun cachedCivilizationDao(): CachedCivilizationDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        const val DATABASE_NAME = "langoa_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_lessons ADD COLUMN cachedExercisesAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pending_sync (
                        id TEXT NOT NULL PRIMARY KEY,
                        operationType TEXT NOT NULL,
                        payload TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        retryCount INTEGER NOT NULL DEFAULT 0,
                        nextRetryAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
