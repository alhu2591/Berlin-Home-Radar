package com.berlin.homeradar

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.Configuration
import com.berlin.homeradar.data.alerts.AppNotificationManager
import com.berlin.homeradar.data.config.RemoteConfigManager
import com.berlin.homeradar.data.preferences.AppSettingsApplier
import com.berlin.homeradar.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltAndroidApp
class BerlinHomeRadarApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var appSettingsApplier: AppSettingsApplier

    @Inject
    lateinit var notificationManager: AppNotificationManager

    @Inject
    lateinit var remoteConfigManager: RemoteConfigManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.ensureChannel()
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            remoteConfigManager.loadCachedConfig()
            remoteConfigManager.refreshIfStale()
            syncScheduler.restoreSchedulingFromSettings()
            val settings = syncScheduler.currentSettings()
            appSettingsApplier.applyLanguage(settings.language)
            appSettingsApplier.applyTheme(settings.themeMode)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
