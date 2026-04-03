package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(listingId: Long) = repository.toggleFavorite(listingId)
}
