package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لحذف بحث محفوظ بمعرّفه.
 *
 * @param id معرّف البحث المحفوظ (UUID).
 */
class DeleteSavedSearchUseCase @Inject constructor(private val repository: HousingRepository) {
    suspend operator fun invoke(id: String) = repository.deleteSavedSearch(id)
}
