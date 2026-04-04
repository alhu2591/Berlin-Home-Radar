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

/**
 * نقطة دخول التطبيق الرئيسية (Application class).
 *
 * مهام هذا الكلاس عند بدء التطبيق:
 * - تهيئة قناة الإشعارات (Notification Channel).
 * - تحميل إعدادات Remote Config المحفوظة مسبقاً ثم تحديثها إن كانت قديمة.
 * - استعادة جدولة المزامنة التلقائية بناءً على إعدادات المستخدم المحفوظة.
 * - تطبيق إعدادات اللغة والثيم على الفور دون الحاجة لإعادة تشغيل.
 *
 * يستخدم [Configuration.Provider] لتمرير [HiltWorkerFactory] إلى WorkManager
 * حتى تتمكن الـ Workers من الاستفادة من Hilt Dependency Injection.
 */
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
        // إنشاء قناة الإشعارات مبكراً حتى تكون جاهزة قبل أي تنبيه
        notificationManager.ensureChannel()
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            // تحميل Remote Config من الكاش أولاً لتجنب التأخير، ثم تحديثه في الخلفية
            remoteConfigManager.loadCachedConfig()
            remoteConfigManager.refreshIfStale()
            // استعادة جدولة WorkManager بناءً على الإعدادات المحفوظة
            syncScheduler.restoreSchedulingFromSettings()
            val settings = syncScheduler.currentSettings()
            // تطبيق اللغة والثيم فوراً عند بدء التطبيق
            appSettingsApplier.applyLanguage(settings.language)
            appSettingsApplier.applyTheme(settings.themeMode)
        }
    }

    /**
     * يوفر إعداد WorkManager المخصص الذي يدمج Hilt لحقن التبعيات داخل Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
