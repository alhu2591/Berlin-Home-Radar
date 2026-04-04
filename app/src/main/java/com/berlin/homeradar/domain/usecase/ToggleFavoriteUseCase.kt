package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case لعكس حالة المفضلة لإعلان محدد.
 *
 * @param listingId المعرّف الداخلي للإعلان في قاعدة البيانات.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(listingId: Long) = repository.toggleFavorite(listingId)
}
