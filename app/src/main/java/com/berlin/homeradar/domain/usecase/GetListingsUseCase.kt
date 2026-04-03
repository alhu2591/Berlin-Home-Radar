package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    operator fun invoke(
        onlyFavorites: Boolean,
        minRooms: Double?,
        district: String?,
    ) = repository.observeListings(
        onlyFavorites = onlyFavorites,
        minRooms = minRooms,
        district = district,
    )
}
