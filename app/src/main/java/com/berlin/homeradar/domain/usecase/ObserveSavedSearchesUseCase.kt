package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class ObserveSavedSearchesUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    operator fun invoke() = repository.observeSavedSearches()
}
