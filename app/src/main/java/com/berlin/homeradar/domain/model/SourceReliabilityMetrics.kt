package com.berlin.homeradar.domain.model

/**
 * مقاييس الموثوقية والأداء لمصدر إعلانات واحد.
 *
 * تُجمَع هذه البيانات تلقائياً عبر كل عملية مزامنة وتُخزَّن في جدول [source_metric].
 * تُستخدم في واجهة إدارة المصادر لإظهار صحة كل مصدر وتاريخ أدائه.
 *
 * @property sourceId معرّف المصدر المرتبط بهذه المقاييس.
 * @property successCount عدد عمليات الجلب الناجحة منذ التثبيت.
 * @property failureCount عدد عمليات الجلب الفاشلة منذ التثبيت.
 * @property lastAttemptMillis وقت آخر محاولة جلب (ناجحة أو فاشلة).
 * @property lastSuccessfulPullMillis وقت آخر جلب ناجح انتهى بنتائج.
 * @property lastItemCount عدد الإعلانات في آخر جلب ناجح.
 * @property averageItemCount متوسط عدد الإعلانات عبر كل عمليات الجلب.
 * @property averageDurationMillis متوسط مدة الجلب بالميلي ثانية.
 * @property consecutiveZeroItemPulls عدد المرات المتتالية التي عاد فيها الجلب بصفر نتائج
 *   (مؤشر محتمل على تغيير في بنية الموقع أو توقفه).
 * @property lastErrorMessage رسالة الخطأ من آخر عملية فاشلة، أو null إذا لم يحدث خطأ.
 *
 * **الخصائص المحسوبة:**
 * - [totalAttempts]: إجمالي محاولات الجلب (ناجحة + فاشلة).
 * - [successRatePercent]: نسبة النجاح كعدد صحيح بين 0 و100.
 * - [hasZeroResultsAnomaly]: تنبيه عندما يعود المصدر بصفر نتائج مرتين متتاليتين أو أكثر.
 */
data class SourceReliabilityMetrics(
    val sourceId: String,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val lastAttemptMillis: Long? = null,
    val lastSuccessfulPullMillis: Long? = null,
    val lastItemCount: Int = 0,
    val averageItemCount: Double = 0.0,
    val averageDurationMillis: Double = 0.0,
    val consecutiveZeroItemPulls: Int = 0,
    val lastErrorMessage: String? = null,
) {
    /** إجمالي عدد محاولات الجلب (ناجحة + فاشلة). */
    val totalAttempts: Int get() = successCount + failureCount

    /** نسبة نجاح الجلب كعدد صحيح من 0 إلى 100. تُرجع 0 إذا لم تحدث أي محاولة بعد. */
    val successRatePercent: Int get() = if (totalAttempts == 0) 0 else ((successCount * 100.0) / totalAttempts).toInt()

    /**
     * true إذا عاد المصدر بصفر نتائج في مرتين متتاليتين أو أكثر،
     * وهو مؤشر على احتمال تغيير في بنية الموقع أو مشكلة في الجلب.
     */
    val hasZeroResultsAnomaly: Boolean get() = consecutiveZeroItemPulls >= 2
}
