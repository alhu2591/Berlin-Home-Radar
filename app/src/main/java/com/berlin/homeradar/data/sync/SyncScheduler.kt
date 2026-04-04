package com.berlin.homeradar.data.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.SyncIntervalOption
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    suspend fun restoreSchedulingFromSettings() {
        applySettings(userPreferencesRepository.appSettingsSnapshot())
    }

    suspend fun currentSettings(): AppSettings = userPreferencesRepository.appSettingsSnapshot()

    internal suspend fun applyStoredSettings() {
        restoreSchedulingFromSettings()
    }

    internal fun applySettings(settings: AppSettings) {
        if (settings.backgroundSyncEnabled) {
            schedulePeriodicSync(settings.syncInterval)
        } else {
            cancelPeriodicSync()
        }
    }

    fun schedulePeriodicSync(interval: SyncIntervalOption = SyncIntervalOption.MINUTES_15) {
        if (interval.minutes == null) {
            cancelPeriodicSync()
            return
        }

        val request = PeriodicWorkRequestBuilder<ListingsSyncWorker>(
            repeatInterval = interval.minutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(Constraints.Builder().build())
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
