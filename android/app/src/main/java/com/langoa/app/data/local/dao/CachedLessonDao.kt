package com.langoa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langoa.app.data.local.entity.CachedLesson
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedLessonDao {
    @Query("SELECT * FROM cached_lessons WHERE languageCode = :languageCode ORDER BY unitNumber, lessonNumber")
    fun getLessonsByLanguage(languageCode: String): Flow<List<CachedLesson>>

    @Query("SELECT * FROM cached_lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): CachedLesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CachedLesson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CachedLesson>)

    @Query("UPDATE cached_lessons SET isCompleted = 1 WHERE id = :lessonId")
    suspend fun markLessonCompleted(lessonId: String)

    @Query("DELETE FROM cached_lessons WHERE languageCode = :languageCode")
    suspend fun clearLessonsForLanguage(languageCode: String)

    @Query("SELECT * FROM cached_lessons WHERE languageCode = :languageCode AND cachedAt > :since ORDER BY unitNumber, lessonNumber")
    suspend fun getFreshLessons(languageCode: String, since: Long): List<CachedLesson>
}
