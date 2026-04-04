package com.berlin.homeradar.domain.filter

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset

/**
 * يتحقق إذا كان الإعلان مطابقاً لمجموعة الفلاتر المحددة.
 *
 * تُستخدم هذه الدالة في سياقين:
 * 1. **تقييم التنبيهات**: في [com.berlin.homeradar.data.alerts.SavedSearchAlertEvaluator]
 *    لمطابقة الإعلانات الجديدة مع البحوث المحفوظة.
 * 2. **الفلترة في الذاكرة**: كـ fallback عند الحاجة للفلترة خارج قاعدة البيانات.
 *
 * **ملاحظة أداء**: الفلترة الرئيسية تحدث على مستوى SQL في [HousingListingDao]
 * وهي أسرع بكثير من هذه الدالة. تُستخدم هذه الدالة فقط حيث لا يتوفر Room query مباشر.
 *
 * @receiver الإعلان المراد فحصه.
 * @param filter مجموعة الفلاتر المراد تطبيقها.
 * @return true إذا اجتاز الإعلان جميع الفلاتر.
 */
fun HousingListing.matchesFilter(filter: ListingFilterPreset): Boolean {
    val matchesQuery = filter.query.isBlank() || searchableText()
        .contains(filter.query, ignoreCase = true)
    val matchesRooms = filter.minRooms == null || rooms >= filter.minRooms
    val matchesArea = filter.minArea == null || areaSqm >= filter.minArea
    val matchesPrice = filter.maxPrice == null || priceEuro <= filter.maxPrice
    val matchesDistrict = filter.district.isNullOrBlank() || district.equals(filter.district, ignoreCase = true)
    val matchesSource = filter.selectedSourceIds.isEmpty() || source in filter.selectedSourceIds
    val matchesJobcenter = !filter.onlyJobcenter || isJobcenterSuitable
    val matchesWohngeld = !filter.onlyWohngeld || isWohngeldEligible
    val matchesWbs = !filter.onlyWbs || isWbsRequired
    val matchesFavorites = !filter.showFavoritesOnly || isFavorite
    return matchesQuery &&
        matchesRooms &&
        matchesArea &&
        matchesPrice &&
        matchesDistrict &&
        matchesSource &&
        matchesJobcenter &&
        matchesWohngeld &&
        matchesWbs &&
        matchesFavorites
}

/**
 * يجمع الحقول النصية القابلة للبحث في سلسلة واحدة.
 *
 * يشمل: العنوان، الموقع، الحي، واسم المصدر.
 * يُستخدم داخلياً في [matchesFilter] للبحث النصي الحر.
 */
private fun HousingListing.searchableText(): String = buildString {
    append(title)
    append(' ')
    append(location)
    append(' ')
    append(district)
    append(' ')
    append(source)
}
