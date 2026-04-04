package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لمراقبة قائمة البحوث المحفوظة.
 *
 * يُرجع Flow يتحدث تلقائياً عند إضافة أو حذف أو تعديل أي بحث محفوظ.
 */
class ObserveSavedSearchesUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    operator fun invoke() = repository.observeSavedSearches()
}
