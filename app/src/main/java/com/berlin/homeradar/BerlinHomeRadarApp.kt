package com.berlin.homeradar

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.berlin.homeradar.data.alerts.AppNotificationManager
import com.berlin.homeradar.data.preferences.AppSettingsApplier
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class BerlinHomeRadarApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var appSettingsApplier: AppSettingsApplier

    @Inject
    lateinit var notificationManager: AppNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.ensureChannel()
        runBlocking {
            val settings = userPreferencesRepository.appSettings.first()
            appSettingsApplier.applyLanguage(settings.language)
            appSettingsApplier.applyTheme(settings.themeMode)
        }
        syncScheduler.ensurePeriodicSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
