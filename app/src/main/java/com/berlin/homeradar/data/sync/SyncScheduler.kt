package com.berlin.homeradar.data.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    fun ensurePeriodicSync() {
        CoroutineScope(Dispatchers.Default).launch {
            if (userPreferencesRepository.backgroundSyncEnabled.first()) {
                schedulePeriodicSync()
            } else {
                cancelPeriodicSync()
            }
        }
    }

    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<ListingsSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ListingsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelPeriodicSync(): Operation {
        return workManager.cancelUniqueWork(ListingsSyncWorker.WORK_NAME)
    }

    fun manualRefresh() {
        val request = OneTimeWorkRequestBuilder<ListingsSyncWorker>().build()
        workManager.enqueue(request)
    }
}
