package com.berlin.homeradar.domain.usecase

import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject

/**
 * Use case موحّد لجميع عمليات تعديل إعدادات التطبيق والمصادر والنسخ الاحتياطية.
 *
 * يجمع عمليات متعددة في كلاس واحد لأنها جميعاً تتعلق بتحديث الإعدادات
 * ولا تستحق كل منها كلاساً منفصلاً. تُفوَّض جميعها مباشرةً إلى [HousingRepository].
 */
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

    /** يُصدّر جميع إعدادات المستخدم كـ JSON نصي. */
    suspend fun exportBackupJson(): String = repository.exportBackupJson()

    /** يستورد إعدادات من نسخة احتياطية JSON ويستبدل الحالية. */
    suspend fun importBackupJson(json: String): Result<Unit> = repository.importBackupJson(json)
}
