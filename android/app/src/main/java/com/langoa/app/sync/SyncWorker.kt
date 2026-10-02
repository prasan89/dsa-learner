package com.langoa.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.langoa.app.data.local.dao.CachedLessonDao
import com.langoa.app.data.local.dao.PendingSyncDao
import com.langoa.app.data.remote.api.LearningApi
import com.langoa.app.data.remote.model.LessonCompletionRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingSyncDao: PendingSyncDao,
    private val cachedLessonDao: CachedLessonDao,
    private val learningApi: LearningApi,
    private val gson: Gson
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val ready = pendingSyncDao.getReady(now)

        var allSucceeded = true
        for (item in ready) {
            try {
                when (item.operationType) {
                    "COMPLETE_LESSON" -> {
                        val payload = gson.fromJson(item.payload, Map::class.java)
                        val languageCode = payload["languageCode"] as String
                        val lessonId = payload["lessonId"] as String
                        val score = (payload["score"] as Double).toInt()
                        learningApi.completeLesson(languageCode, lessonId, LessonCompletionRequest(score))
                        cachedLessonDao.markLessonCompleted(lessonId)
                        pendingSyncDao.delete(item.id)
                    }
                }
            } catch (e: Exception) {
                val newRetryCount = item.retryCount + 1
                if (newRetryCount >= MAX_RETRIES) {
                    pendingSyncDao.delete(item.id)
                } else {
                    val backoffMs = BACKOFF_BASE_MS * (1L shl newRetryCount)
                    pendingSyncDao.incrementRetry(item.id, now + backoffMs)
                    allSucceeded = false
                }
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }

    companion object {
        private const val MAX_RETRIES = 5
        private const val BACKOFF_BASE_MS = 30_000L
        const val WORK_NAME = "langoa_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
