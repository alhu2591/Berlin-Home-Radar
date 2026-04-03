package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class RefreshListingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(trigger: String): Result<Unit> = repository.refreshListings(trigger)
}
