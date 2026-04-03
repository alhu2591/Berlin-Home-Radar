package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class UpdateSyncSettingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        repository.setBackgroundSyncEnabled(enabled)
    }

    suspend fun setRemoteSourceEnabled(enabled: Boolean) {
        repository.setRemoteSourceEnabled(enabled)
    }
}
