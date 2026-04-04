package com.berlin.homeradar.presentation.screen.listings

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SyncInfo

/**
 * الحالة الكاملة لشاشة قائمة الإعلانات.
 *
 * كائن غير قابل للتعديل (immutable) يُمثّل صورة لحظية للشاشة.
 * يُبنى في [ListingsViewModel] ويُرسل للـ Composables عبر [StateFlow].
 *
 * ## التصميم:
 * جميع القيم افتراضية مُعرَّفة لتُمكّن من إنشاء حالة أولية فارغة
 * قبل وصول أي بيانات من قاعدة البيانات.
 *
 * @property listings الإعلانات المُصفَّاة الظاهرة حالياً في القائمة.
 * @property syncInfo معلومات المزامنة الأخيرة وإعداداتها.
 * @property showFavoritesOnly هل فلتر المفضلة مفعّل.
 * @property query نص البحث الحر المُدخَل من المستخدم.
 * @property minRooms الحد الأدنى للغرف في الفلتر الحالي.
 * @property minArea الحد الأدنى للمساحة بالمتر المربع في الفلتر الحالي.
 * @property maxPrice الحد الأقصى للسعر باليورو في الفلتر الحالي.
 * @property district الحي المُختار للفلترة، أو null لعرض كل الأحياء.
 * @property selectedSourceIds المصادر المُختارة للفلترة (فارغة = كل المصادر).
 * @property onlyJobcenter هل فلتر "مناسب للـ Jobcenter" مفعّل.
 * @property onlyWohngeld هل فلتر "يقبل Wohngeld" مفعّل.
 * @property onlyWbs هل فلتر "يستلزم WBS" مفعّل.
 * @property availableDistricts قائمة الأحياء المتاحة مشتقة من الإعلانات النشطة.
 * @property availableSources قائمة أسماء المصادر المتاحة مشتقة من الإعلانات النشطة.
 * @property savedSearches البحوث المحفوظة للمستخدم.
 * @property activeAlertsCount عدد البحوث المحفوظة ذات التنبيهات المفعّلة مع مطابقات حالية.
 * @property isRefreshing هل المزامنة جارية الآن (لعرض مؤشر التحميل).
 * @property hasActiveFilters هل هناك أي فلتر غير افتراضي مُطبَّق حالياً.
 * @property syncIssueMessage رسالة خطأ المزامنة إن وُجد، أو null.
 */
data class ListingsUiState(
    val listings: List<HousingListing> = emptyList(),
    val syncInfo: SyncInfo = SyncInfo(),
    val showFavoritesOnly: Boolean = false,
    val query: String = "",
    val minRooms: Double? = null,
    val minArea: Double? = null,
    val maxPrice: Int? = null,
    val district: String? = null,
    val selectedSourceIds: Set<String> = emptySet(),
    val onlyJobcenter: Boolean = false,
    val onlyWohngeld: Boolean = false,
    val onlyWbs: Boolean = false,
    val availableDistricts: List<String> = emptyList(),
    val availableSources: List<String> = emptyList(),
    val savedSearches: List<SavedSearch> = emptyList(),
    val activeAlertsCount: Int = 0,
    val isRefreshing: Boolean = false,
    val hasActiveFilters: Boolean = false,
    val syncIssueMessage: String? = null,
)
