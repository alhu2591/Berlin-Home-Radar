package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لجلب ومراقبة قائمة الإعلانات.
 *
 * يُوفّر واجهتين:
 * - [invoke]: مراقبة الإعلانات المُصفَّاة بفلاتر محددة (يُستخدم في شاشة القائمة الرئيسية).
 * - [allActive]: مراقبة جميع الإعلانات النشطة بلا فلاتر (لاشتقاق خيارات الفلترة المتاحة).
 */
class GetListingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    /** يُرجع Flow بالإعلانات المطابقة لـ [filter]، يتحدث تلقائياً عند تغيير البيانات. */
    operator fun invoke(filter: ListingFilterPreset) = repository.observeListings(filter)

    /** يُرجع Flow بجميع الإعلانات النشطة بلا فلاتر. */
    fun allActive() = repository.observeAllActiveListings()
}
