package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لتشغيل مزامنة الإعلانات من جميع المصادر المفعّلة.
 *
 * @param trigger سبب المزامنة للتتبع (مثل "manual", "workmanager").
 * @return [Result.success] عند نجاح جزء على الأقل، [Result.failure] عند فشل كلي.
 */
class RefreshListingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(trigger: String): Result<Unit> = repository.refreshListings(trigger)
}
