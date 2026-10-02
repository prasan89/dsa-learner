package com.langoa.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.langoa.app.auth.AuthEvent
import com.langoa.app.auth.AuthEventBus
import com.langoa.app.data.local.dao.PendingSyncDao
import com.langoa.app.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LangoaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var authEventBus: AuthEventBus

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var pendingSyncDao: PendingSyncDao

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        observeAuthEvents()
        enqueueSyncIfPending()
    }

    private fun observeAuthEvents() {
        appScope.launch {
            authEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.SessionExpired -> {
                        // MainActivity observes the same bus and navigates to Login.
                    }
                }
            }
        }
    }

    private fun enqueueSyncIfPending() {
        appScope.launch(Dispatchers.IO) {
            if (pendingSyncDao.count() > 0) {
                SyncWorker.enqueue(this@LangoaApp)
            }
        }
    }
}
