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

/**
 * يُدير جدولة مزامنة الإعلانات في الخلفية عبر WorkManager.
 *
 * ## مسؤوليات هذا الكلاس:
 * - جدولة [ListingsSyncWorker] بشكل دوري بناءً على إعدادات المستخدم.
 * - استعادة الجدولة عند إعادة تشغيل التطبيق (تُستدعى من [BerlinHomeRadarApp]).
 * - إلغاء الجدولة عند تعطيل المزامنة التلقائية.
 * - تشغيل مزامنة فورية يدوية عند طلب المستخدم.
 *
 * ## سياسة [ExistingPeriodicWorkPolicy.UPDATE]:
 * عند تغيير الفترة، يُستبدَل الـ Worker القديم بالجديد فوراً بدلاً من انتظار انتهاء الدورة الحالية.
 *
 * @constructor يُحقن بواسطة Hilt كـ Singleton.
 */
@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    /**
     * يُعيد تطبيق جدولة المزامنة بناءً على الإعدادات المحفوظة.
     * يُستدعى عند بدء التطبيق لاستعادة الحالة الصحيحة بعد إعادة التشغيل.
     */
    suspend fun restoreSchedulingFromSettings() {
        applySettings(userPreferencesRepository.appSettingsSnapshot())
    }

    /**
     * يُرجع snapshot من إعدادات المستخدم الحالية.
     * يُستخدم لقراءة الإعدادات مرة واحدة (غير تفاعلي).
     */
    suspend fun currentSettings(): AppSettings = userPreferencesRepository.appSettingsSnapshot()

    /** @suppress داخلي للاختبارات. */
    internal suspend fun applyStoredSettings() {
        restoreSchedulingFromSettings()
    }

    /**
     * يُطبّق إعدادات الجدولة مباشرةً من كائن [AppSettings].
     * يُفعّل أو يُلغي الجدولة بناءً على [AppSettings.backgroundSyncEnabled].
     *
     * @param settings الإعدادات المراد تطبيقها.
     */
    internal fun applySettings(settings: AppSettings) {
        if (settings.backgroundSyncEnabled) {
            schedulePeriodicSync(settings.syncInterval)
        } else {
            cancelPeriodicSync()
        }
    }

    /**
     * يجدول [ListingsSyncWorker] بشكل دوري بالفترة المحددة.
     *
     * إذا كانت [interval] هي [SyncIntervalOption.MANUAL]، تُلغى الجدولة التلقائية.
     *
     * @param interval فترة التكرار. الافتراضي 15 دقيقة.
     */
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

        // UPDATE: يستبدل الجدولة الحالية فوراً بدلاً من الانتظار لانتهائها
        workManager.enqueueUniquePeriodicWork(
            ListingsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /**
     * يُلغي الجدولة الدورية للمزامنة.
     *
     * @return [Operation] يمكن مراقبته لمعرفة وقت اكتمال الإلغاء.
     */
    fun cancelPeriodicSync(): Operation {
        return workManager.cancelUniqueWork(ListingsSyncWorker.WORK_NAME)
    }

    /**
     * يُشغّل مزامنة فورية واحدة بشكل مستقل عن الجدول الدوري.
     * يُستدعى عند ضغط المستخدم على زر التحديث اليدوي.
     */
    fun manualRefresh() {
        val request = OneTimeWorkRequestBuilder<ListingsSyncWorker>().build()
        workManager.enqueue(request)
    }
}
