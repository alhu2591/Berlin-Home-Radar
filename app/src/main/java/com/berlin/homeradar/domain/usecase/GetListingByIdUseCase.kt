package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لجلب إعلان واحد بمعرّفه الداخلي.
 *
 * @param id المعرّف الداخلي في قاعدة البيانات.
 * @return الإعلان إن وُجد، أو null.
 */
class GetListingByIdUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.getListingById(id)
}
