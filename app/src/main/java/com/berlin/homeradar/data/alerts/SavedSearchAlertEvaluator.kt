package com.berlin.homeradar.data.alerts

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.filter.matchesFilter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * يُقيّم البحوث المحفوظة ذات التنبيهات المفعّلة ويُرسل إشعاراً عند ظهور إعلانات جديدة مطابقة.
 *
 * ## آلية العمل:
 * 1. جلب جميع الإعلانات النشطة من قاعدة البيانات.
 * 2. جلب البحوث المحفوظة التي فعّل المستخدم تنبيهاتها.
 * 3. لكل بحث: مقارنة الإعلانات المطابقة مع قائمة الإعلانات التي سبق إشعار المستخدم بها.
 * 4. إرسال إشعار فقط عن الإعلانات **الجديدة** (غير المسبوق إشعاره عنها).
 * 5. تحديث قائمة الإعلانات "المشاهَدة" في DataStore لتجنب تكرار الإشعار.
 *
 * ## متى يُستدعى:
 * يُستدعى تلقائياً من [com.berlin.homeradar.data.sync.ListingsSyncWorker] بعد كل مزامنة ناجحة.
 *
 * @constructor يُحقن بواسطة Hilt كـ Singleton.
 */
@Singleton
class SavedSearchAlertEvaluator @Inject constructor(
    private val housingListingDao: HousingListingDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationManager: AppNotificationManager,
) {

    /**
     * يُشغّل دورة التقييم الكاملة ويُرسل الإشعارات المناسبة.
     *
     * العملية مُقاومة للأخطاء: فشل إشعار واحد لا يوقف تقييم البقية.
     * لا تُكتب أي بيانات في DataStore إذا لم تكن هناك إعلانات جديدة ([changed] = false).
     */
    suspend fun evaluateAndNotify() {
        val listings = housingListingDao.getAllActive().map { it.toDomain() }
        val savedSearches = userPreferencesRepository.appSettingsSnapshot().savedSearches
        val seenBySearch = userPreferencesRepository.getSeenAlertMatches().toMutableMap()
        var changed = false

        savedSearches.filter { it.alertsEnabled }.forEach { savedSearch ->
            val matches = listings.filter { it.matchesFilter(savedSearch.filters) }
            val previous = seenBySearch[savedSearch.id].orEmpty()
            // فقط الإعلانات التي لم يُشعَر المستخدم بها من قبل لهذا البحث
            val newMatches = matches.filter { it.id !in previous }
            if (newMatches.isNotEmpty()) {
                notificationManager.showSavedSearchAlert(
                    searchName = savedSearch.name,
                    matchCount = newMatches.size,
                    listing = newMatches.first(),
                )
                seenBySearch[savedSearch.id] = (previous + newMatches.map { it.id }).toSet()
                changed = true
            }
        }

        // حفظ الحالة فقط إذا كان هناك تغيير فعلي لتجنب كتابات DataStore غير ضرورية
        if (changed) {
            userPreferencesRepository.markSeenAlertMatches(seenBySearch)
        }
    }
}
