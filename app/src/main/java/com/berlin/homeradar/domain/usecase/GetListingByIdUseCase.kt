package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class GetListingByIdUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.getListingById(id)
}
