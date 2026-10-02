package com.langoa.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langoa.app.data.local.entity.PendingSync

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: PendingSync)

    @Query("SELECT * FROM pending_sync WHERE nextRetryAt <= :currentTime ORDER BY createdAt ASC")
    suspend fun getReady(currentTime: Long): List<PendingSync>

    @Query("SELECT COUNT(*) FROM pending_sync")
    suspend fun count(): Int

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE pending_sync SET retryCount = retryCount + 1, nextRetryAt = :nextRetryAt WHERE id = :id")
    suspend fun incrementRetry(id: String, nextRetryAt: Long)
}
