package com.berlin.homeradar.domain.model

/**
 * حالات الفحص الممكنة لصحة مصدر الإعلانات.
 */
enum class SourceHealthStatus {
    /** لم يُجرَ أي فحص بعد على هذا المصدر. */
    IDLE,
    /** الفحص جارٍ الآن. */
    CHECKING,
    /** الفحص الأخير نجح والمصدر يعمل. */
    SUCCESS,
    /** الفحص الأخير فشل (تحقق من [SourceHealth.message] للتفاصيل). */
    FAILED,
    /** هذا المصدر لا يدعم الفحص التلقائي (مصادر CATALOG للاستعراض اليدوي فقط). */
    UNSUPPORTED,
}

/**
 * نتيجة فحص صحة مصدر واحد.
 *
 * @property sourceId معرّف المصدر الذي جُرّب.
 * @property status حالة الفحص الحالية.
 * @property message رسالة وصفية (خطأ عند FAILED، معلومات عند SUCCESS)، أو null.
 * @property checkedAtMillis وقت آخر فحص بالميلي ثانية، أو null إذا لم يُفحص بعد.
 */
data class SourceHealth(
    val sourceId: String,
    val status: SourceHealthStatus,
    val message: String? = null,
    val checkedAtMillis: Long? = null,
)
