package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    operator fun invoke(filter: ListingFilterPreset) = repository.observeListings(filter)
    fun allActive() = repository.observeAllActiveListings()
}
