package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class SaveSearchUseCase @Inject constructor(private val repository: HousingRepository) {
    suspend operator fun invoke(search: SavedSearch) = repository.saveSearch(search)
}
