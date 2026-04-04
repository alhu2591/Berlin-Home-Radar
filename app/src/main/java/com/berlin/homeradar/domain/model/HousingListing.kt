package com.berlin.homeradar.domain.model

/**
 * نموذج البيانات الرئيسي لإعلان السكن في طبقة الـ Domain.
 *
 * هذا الكلاس يمثل إعلاناً واحداً كما يراه المستخدم، بعيداً عن تفاصيل قاعدة البيانات.
 *
 * @property id المعرّف الداخلي التلقائي في قاعدة البيانات (0 يعني لم يُحفظ بعد).
 * @property source اسم المصدر الذي جُلب منه الإعلان (مثل "wbm", "degewo").
 * @property externalId المعرّف الخارجي للإعلان في موقعه الأصلي، يُستخدم مع [source] للكشف عن التكرار.
 * @property title عنوان الإعلان كما ظهر في المصدر.
 * @property priceEuro الإيجار الشهري باليورو.
 * @property district الحي أو المنطقة في برلين (مثل "Mitte", "Spandau").
 * @property location وصف الموقع التفصيلي (عنوان الشارع أو وصف حر).
 * @property rooms عدد الغرف (قد يكون كسراً مثل 1.5 أو 2.5).
 * @property areaSqm المساحة بالمتر المربع.
 * @property imageUrl رابط الصورة الرئيسية للإعلان، أو null إذا لا توجد صورة.
 * @property listingUrl رابط الإعلان الأصلي في الموقع المصدر.
 * @property isJobcenterSuitable هل الشقة مقبولة من قِبل Jobcenter (تناسب حاملي Bürgergeld/ALG II).
 * @property isWohngeldEligible هل يمكن الحصول على Wohngeld لهذه الشقة.
 * @property isWbsRequired هل تستلزم الشقة وثيقة WBS (Wohnberechtigungsschein).
 * @property isFavorite هل أضاف المستخدم هذا الإعلان للمفضلة.
 * @property updatedAtEpochMillis وقت آخر تعديل على الإعلان بالميلي ثانية (epoch).
 * @property lastSeenAtEpochMillis آخر مرة رُصد فيها الإعلان نشطاً خلال المزامنة.
 * @property isActive هل الإعلان لا يزال نشطاً ومرئياً للمستخدم.
 * @property lifecycleStatus حالة الإعلان التفصيلية (نشط، مخفي، منتهي... إلخ).
 */
data class HousingListing(
    val id: Long = 0L,
    val source: String,
    val externalId: String,
    val title: String,
    val priceEuro: Int,
    val district: String,
    val location: String,
    val rooms: Double,
    val areaSqm: Double,
    val imageUrl: String?,
    val listingUrl: String,
    val isJobcenterSuitable: Boolean,
    val isWohngeldEligible: Boolean,
    val isWbsRequired: Boolean,
    val isFavorite: Boolean,
    val updatedAtEpochMillis: Long,
    val lastSeenAtEpochMillis: Long = updatedAtEpochMillis,
    val isActive: Boolean = true,
    val lifecycleStatus: ListingLifecycleStatus = ListingLifecycleStatus.ACTIVE,
)
