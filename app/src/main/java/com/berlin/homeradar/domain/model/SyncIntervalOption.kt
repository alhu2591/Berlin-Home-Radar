package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

/**
 * خيارات فترة المزامنة التلقائية في الخلفية.
 *
 * يُسلسَل هذا الـ Enum بـ [storageValue] في DataStore بدلاً من اسمه
 * لضمان استمرارية البيانات عند إعادة تسمية القيم مستقبلاً.
 *
 * @property storageValue القيمة المخزَّنة في DataStore (لا تتغير).
 * @property minutes مدة الفترة بالدقائق، أو null لـ [MANUAL] (لا جدولة تلقائية).
 */
@Serializable
enum class SyncIntervalOption(val storageValue: String, val minutes: Long?) {
    /** لا مزامنة تلقائية، فقط عند طلب المستخدم يدوياً. */
    MANUAL("manual", null),
    MINUTES_15("15m", 15),
    MINUTES_30("30m", 30),
    HOUR_1("1h", 60),
    HOURS_3("3h", 180);

    companion object {
        /**
         * يُعيد الخيار المطابق لـ [value] المخزَّن، أو [MINUTES_15] كقيمة افتراضية آمنة.
         * @param value القيمة المخزَّنة في DataStore.
         */
        fun fromStorage(value: String?): SyncIntervalOption =
            entries.firstOrNull { it.storageValue == value } ?: MINUTES_15
    }
}
