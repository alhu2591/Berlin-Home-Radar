package com.berlin.homeradar.data.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.SyncIntervalOption
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SyncSchedulerTest {
    private val workManager: WorkManager = mock()
    private val userPreferencesRepository: UserPreferencesRepository = mock()
    private val operation: Operation = mock()

    private lateinit var scheduler: SyncScheduler

    @Before
    fun setUp() {
        scheduler = SyncScheduler(
            workManager = workManager,
            userPreferencesRepository = userPreferencesRepository,
        )
    }

    @Test
    fun applyStoredSettings_schedulesPeriodicWorkWhenBackgroundSyncEnabled() = runTest {
        doReturn(flowOf(AppSettings(backgroundSyncEnabled = true, syncInterval = SyncIntervalOption.HOUR_1)))
            .`when`(userPreferencesRepository).appSettings

        scheduler.applyStoredSettings()

        val requestCaptor = argumentCaptor<PeriodicWorkRequest>()
        verify(workManager).enqueueUniquePeriodicWork(
            eq(ListingsSyncWorker.WORK_NAME),
            eq(ExistingPeriodicWorkPolicy.UPDATE),
            requestCaptor.capture(),
        )
    }

    @Test
    fun applyStoredSettings_cancelsPeriodicWorkWhenBackgroundSyncDisabled() = runTest {
        doReturn(flowOf(AppSettings(backgroundSyncEnabled = false)))
            .`when`(userPreferencesRepository).appSettings
        doReturn(operation).`when`(workManager).cancelUniqueWork(ListingsSyncWorker.WORK_NAME)

        scheduler.applyStoredSettings()

        verify(workManager).cancelUniqueWork(ListingsSyncWorker.WORK_NAME)
    }

    @Test
    fun schedulePeriodicSync_cancelsWorkForManualMode() {
        doReturn(operation).`when`(workManager).cancelUniqueWork(ListingsSyncWorker.WORK_NAME)

        scheduler.schedulePeriodicSync(SyncIntervalOption.MANUAL)

        verify(workManager).cancelUniqueWork(ListingsSyncWorker.WORK_NAME)
    }

    @Test
    fun manualRefresh_enqueuesOneTimeWorkRequest() {
        scheduler.manualRefresh()

        verify(workManager).enqueue(any<OneTimeWorkRequest>())
    }
}
