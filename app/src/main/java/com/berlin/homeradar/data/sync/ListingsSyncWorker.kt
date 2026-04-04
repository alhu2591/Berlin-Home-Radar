package com.berlin.homeradar.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.berlin.homeradar.data.alerts.SavedSearchAlertEvaluator
import com.berlin.homeradar.domain.usecase.RefreshListingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker المسؤول عن تنفيذ مزامنة الإعلانات في الخلفية عبر WorkManager.
 *
 * يُشغَّل تلقائياً بالفترة المحددة في [SyncScheduler]، أو يدوياً عبر [SyncScheduler.manualRefresh].
 *
 * ## تسلسل العمليات:
 * 1. جلب الإعلانات من جميع المصادر المفعّلة عبر [RefreshListingsUseCase].
 * 2. عند النجاح: تقييم البحوث المحفوظة وإرسال تنبيهات للمطابقات الجديدة.
 * 3. عند الفشل: إرجاع [Result.retry] ليُعيد WorkManager المحاولة تلقائياً.
 *
 * يستخدم [@HiltWorker] مع [@AssistedInject] لحقن التبعيات عبر Hilt داخل WorkManager.
 *
 * @see SyncScheduler لإدارة الجدولة وإلغائها.
 */
@HiltWorker
class ListingsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val refreshListingsUseCase: RefreshListingsUseCase,
    private val savedSearchAlertEvaluator: SavedSearchAlertEvaluator,
) : CoroutineWorker(appContext, params) {

    /**
     * ينفّذ دورة المزامنة الكاملة.
     *
     * @return [Result.success] عند نجاح الجلب (حتى لو بعض المصادر فشلت جزئياً).
     *         [Result.retry] عند فشل كلي يستوجب إعادة المحاولة.
     */
    override suspend fun doWork(): Result {
        return refreshListingsUseCase("workmanager")
            .fold(
                onSuccess = {
                    // تقييم البحوث المحفوظة وإرسال الإشعارات بعد نجاح الجلب مباشرةً
                    savedSearchAlertEvaluator.evaluateAndNotify()
                    Result.success()
                },
                onFailure = { Result.retry() },
            )
    }

    companion object {
        /** الاسم الفريد للـ Worker في WorkManager، يُستخدم لتحديثه أو إلغائه. */
        const val WORK_NAME = "listings_sync_worker"
    }
}
