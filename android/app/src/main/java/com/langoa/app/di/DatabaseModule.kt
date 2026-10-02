package com.langoa.app.di

import android.content.Context
import androidx.room.Room
import com.langoa.app.data.local.LangoaDatabase
import com.langoa.app.data.local.dao.CachedCivilizationDao
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.dao.PendingSyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLangoaDatabase(@ApplicationContext context: Context): LangoaDatabase {
        return Room.databaseBuilder(
            context,
            LangoaDatabase::class.java,
            LangoaDatabase.DATABASE_NAME
        )
            .addMigrations(LangoaDatabase.MIGRATION_2_3, LangoaDatabase.MIGRATION_3_4, LangoaDatabase.MIGRATION_4_5)
            .build()
    }

    @Provides
    @Singleton
    fun provideCachedLessonDao(database: LangoaDatabase): CachedLessonDao =
        database.cachedLessonDao()

    @Provides
    @Singleton
    fun provideCachedCivilizationDao(database: LangoaDatabase): CachedCivilizationDao =
        database.cachedCivilizationDao()

    @Provides
    @Singleton
    fun providePendingSyncDao(database: LangoaDatabase): PendingSyncDao =
        database.pendingSyncDao()
}
