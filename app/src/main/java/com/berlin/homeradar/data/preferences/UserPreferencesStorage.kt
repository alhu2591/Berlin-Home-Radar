package com.berlin.homeradar.data.preferences

import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceType
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class UserPreferencesStorage(
    private val json: Json,
) {
    fun buildCustomSource(
        displayName: String,
        websiteUrl: String,
        description: String,
    ): SourceDefinition {
        val normalizedName = displayName.trim()
        val normalizedUrl = websiteUrl.trim()
        val inferredType = inferSourceType(normalizedUrl)

        return SourceDefinition(
            id = "custom-" + normalizedName
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-'),
            displayName = normalizedName,
            websiteUrl = normalizedUrl,
            description = description.trim().ifBlank { USER_ADDED_SOURCE_DESCRIPTION },
            supportsAutomatedSync = true,
            isUserAdded = true,
            sourceType = inferredType,
        )
    }

    fun defaultEnabledSourceIds(remoteEnabled: Boolean): Set<String> {
        val defaults = SourceCatalog.all
            .asSequence()
            .filter { source -> source.supportsAutomatedSync }
            .map { source -> source.id }
            .toMutableSet()
        if (!remoteEnabled) defaults.remove(SourceCatalog.RemoteJson.id)
        return defaults
    }

    fun decodeEnabledIds(raw: String): Set<String> =
        runCatching { json.decodeFromString<List<String>>(raw).toSet() }.getOrDefault(emptySet())

    fun encodeEnabledIds(ids: Set<String>): String = json.encodeToString(ids.sorted())

    fun sanitizeEnabledSourceIds(
        enabledSourceIds: Set<String>,
        remoteEnabled: Boolean,
        customSources: List<SourceDefinition>,
    ): Set<String> {
        val allowedIds = SourceCatalog.supportedSyncSourceIds + customSources
            .asSequence()
            .filter { it.supportsAutomatedSync }
            .map { it.id }
            .toSet()
        return enabledSourceIds.filter { it in allowedIds }.toMutableSet().apply {
            if (!remoteEnabled) remove(SourceCatalog.RemoteJson.id)
        }
    }

    fun decodeCustomSources(raw: String?): List<SourceDefinition> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<SourceDefinition>>(raw) }.getOrDefault(emptyList())
    }

    fun decodeSavedSearches(raw: String?): List<SavedSearch> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<SavedSearch>>(raw) }.getOrDefault(emptyList())
    }

    fun normalizeSourceOrder(rawOrder: String?, customSources: List<SourceDefinition>): List<String> {
        val baseOrder = runCatching { json.decodeFromString<List<String>>(rawOrder ?: "") }.getOrNull().orEmpty()
        return normalizeSourceOrder(baseOrder, customSources)
    }

    fun normalizeSourceOrder(baseOrder: List<String>, customSources: List<SourceDefinition>): List<String> {
        val known = (SourceCatalog.all + customSources).map { it.id }.distinct()
        val normalized = baseOrder.filter { it in known }.distinct().toMutableList()
        known.forEach { if (it !in normalized) normalized.add(it) }
        return normalized
    }

    fun normalizeBackupPayload(payload: BackupPayload): BackupPayload {
        val normalizedCustomSources = payload.customSources.distinctBy { it.id }
        return payload.copy(
            customSources = normalizedCustomSources,
            enabledSourceIds = sanitizeEnabledSourceIds(payload.enabledSourceIds, payload.remoteSourceEnabled, normalizedCustomSources),
            sourceOrder = normalizeSourceOrder(payload.sourceOrder, normalizedCustomSources),
        )
    }

    fun backupPayloadFromSettings(settings: AppSettings): BackupPayload = BackupPayload(
        backgroundSyncEnabled = settings.backgroundSyncEnabled,
        remoteSourceEnabled = settings.remoteSourceEnabled,
        appLanguage = settings.language,
        themeMode = settings.themeMode,
        syncInterval = settings.syncInterval,
        enabledSourceIds = settings.enabledSourceIds,
        customSources = settings.customSources,
        sourceOrder = settings.sourceOrder,
        savedSearches = settings.savedSearches,
    )

    private fun inferSourceType(normalizedUrl: String): SourceType = when {
        normalizedUrl.endsWith(".json", ignoreCase = true) -> SourceType.API
        normalizedUrl.contains("api", ignoreCase = true) -> SourceType.API
        else -> SourceType.HTML
    }

    @Serializable
    data class BackupPayload(
        val backgroundSyncEnabled: Boolean,
        val remoteSourceEnabled: Boolean,
        val appLanguage: AppLanguage,
        val themeMode: ThemeMode,
        val syncInterval: SyncIntervalOption,
        val enabledSourceIds: Set<String>,
        val customSources: List<SourceDefinition>,
        val sourceOrder: List<String>,
        val savedSearches: List<SavedSearch> = emptyList(),
    )

    companion object {
        private const val USER_ADDED_SOURCE_DESCRIPTION = "User-added source"
    }
}
