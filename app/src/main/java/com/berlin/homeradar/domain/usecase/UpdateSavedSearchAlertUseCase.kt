package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لتفعيل أو تعطيل تنبيهات بحث محفوظ.
 *
 * @param id معرّف البحث المحفوظ.
 * @param enabled true لتفعيل التنبيهات، false لإيقافها.
 */
class UpdateSavedSearchAlertUseCase @Inject constructor(private val repository: HousingRepository) {
    suspend operator fun invoke(id: String, enabled: Boolean) = repository.updateSavedSearchAlerts(id, enabled)
}
