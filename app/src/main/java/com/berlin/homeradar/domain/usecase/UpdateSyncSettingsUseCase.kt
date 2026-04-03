package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

class UpdateSyncSettingsUseCase @Inject constructor(
    private val repository: HousingRepository,
) {
    suspend fun setBackgroundSyncEnabled(enabled: Boolean) = repository.setBackgroundSyncEnabled(enabled)
    suspend fun setRemoteSourceEnabled(enabled: Boolean) = repository.setRemoteSourceEnabled(enabled)
    suspend fun setAppLanguage(language: AppLanguage) = repository.setAppLanguage(language)
    suspend fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)
    suspend fun setSyncInterval(option: SyncIntervalOption) = repository.setSyncInterval(option)
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean) = repository.setSourceEnabled(sourceId, enabled)
    suspend fun setAllSupportedSourcesEnabled(enabled: Boolean) = repository.setAllSupportedSourcesEnabled(enabled)
    suspend fun moveSource(sourceId: String, moveUp: Boolean) = repository.moveSource(sourceId, moveUp)
    suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String) = repository.addCustomSource(displayName, websiteUrl, description)
    suspend fun removeCustomSource(sourceId: String) = repository.removeCustomSource(sourceId)
    suspend fun exportBackupJson(): String = repository.exportBackupJson()
    suspend fun importBackupJson(json: String): Result<Unit> = repository.importBackupJson(json)
}
