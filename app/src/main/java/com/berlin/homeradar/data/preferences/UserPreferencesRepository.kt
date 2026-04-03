package com.berlin.homeradar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceType
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("user_prefs.preferences_pb") }
    )

    private object Keys {
        val BackgroundSyncEnabled = booleanPreferencesKey("background_sync_enabled")
        val RemoteSourceEnabled = booleanPreferencesKey("remote_source_enabled")
        val AppLanguage = stringPreferencesKey("app_language")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val SyncInterval = stringPreferencesKey("sync_interval")
        val EnabledSourceIds = stringPreferencesKey("enabled_source_ids")
        val CustomSources = stringPreferencesKey("custom_sources")
        val SourceOrder = stringPreferencesKey("source_order")
        val SavedSearches = stringPreferencesKey("saved_searches")
        val SeenAlertMatches = stringPreferencesKey("seen_alert_matches")
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val remoteEnabled = prefs[Keys.RemoteSourceEnabled] ?: false
        val customSources = decodeCustomSources(prefs[Keys.CustomSources])
        AppSettings(
            language = AppLanguage.fromTag(prefs[Keys.AppLanguage]),
            themeMode = ThemeMode.fromStorage(prefs[Keys.ThemeMode]),
            syncInterval = SyncIntervalOption.fromStorage(prefs[Keys.SyncInterval]),
            backgroundSyncEnabled = prefs[Keys.BackgroundSyncEnabled] ?: true,
            remoteSourceEnabled = remoteEnabled,
            enabledSourceIds = prefs[Keys.EnabledSourceIds]?.let(::decodeEnabledIds)
                ?: defaultEnabledSourceIds(remoteEnabled),
            customSources = customSources,
            sourceOrder = normalizeSourceOrder(
                rawOrder = prefs[Keys.SourceOrder],
                customSources = customSources,
            ),
            savedSearches = decodeSavedSearches(prefs[Keys.SavedSearches]),
        )
    }

    val enabledSourceIds: Flow<Set<String>> = appSettings.map { it.enabledSourceIds }
    val savedSearches: Flow<List<SavedSearch>> = appSettings.map { it.savedSearches }
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[Keys.OnboardingCompleted] ?: false }

    suspend fun appSettingsSnapshot(): AppSettings = appSettings.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.OnboardingCompleted] = completed }
    }

    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BackgroundSyncEnabled] = enabled }
    }

    suspend fun setRemoteSourceEnabled(enabled: Boolean) {
        dataStore.edit {
            it[Keys.RemoteSourceEnabled] = enabled
            val current = it[Keys.EnabledSourceIds]?.let(::decodeEnabledIds) ?: defaultEnabledSourceIds(!enabled)
            val updated = if (enabled) current + SourceCatalog.RemoteJson.id else current - SourceCatalog.RemoteJson.id
            it[Keys.EnabledSourceIds] = encodeEnabledIds(updated)
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { it[Keys.AppLanguage] = language.tag }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.storageValue }
    }

    suspend fun setSyncInterval(option: SyncIntervalOption) {
        dataStore.edit { it[Keys.SyncInterval] = option.storageValue }
    }

    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean) {
        dataStore.edit {
            val current = it[Keys.EnabledSourceIds]?.let(::decodeEnabledIds) ?: emptySet()
            it[Keys.EnabledSourceIds] = encodeEnabledIds(if (enabled) current + sourceId else current - sourceId)
        }
    }

    suspend fun setAllSupportedSourcesEnabled(enabled: Boolean) {
        dataStore.edit {
            val current = it[Keys.EnabledSourceIds]?.let(::decodeEnabledIds) ?: emptySet()
            val customSources = decodeCustomSources(it[Keys.CustomSources])
            val customSyncIds = customSources.filter { source -> source.supportsAutomatedSync }.map { source -> source.id }.toSet()
            val target = SourceCatalog.supportedSyncSourceIds + customSyncIds
            val updated = if (enabled) current + target else current - target
            it[Keys.EnabledSourceIds] = encodeEnabledIds(updated)
        }
    }

    suspend fun moveSource(sourceId: String, moveUp: Boolean) {
        dataStore.edit {
            val customSources = decodeCustomSources(it[Keys.CustomSources])
            val order = normalizeSourceOrder(it[Keys.SourceOrder], customSources).toMutableList()
            val index = order.indexOf(sourceId)
            if (index == -1) return@edit
            val swapIndex = if (moveUp) index - 1 else index + 1
            if (swapIndex !in order.indices) return@edit
            val temp = order[index]
            order[index] = order[swapIndex]
            order[swapIndex] = temp
            it[Keys.SourceOrder] = json.encodeToString(order)
        }
    }

    suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String) {
        dataStore.edit {
            val current = decodeCustomSources(it[Keys.CustomSources]).toMutableList()
            val normalizedUrl = websiteUrl.trim()
            val inferredType = when {
                normalizedUrl.endsWith(".json", ignoreCase = true) -> SourceType.API
                normalizedUrl.contains("api", ignoreCase = true) -> SourceType.API
                else -> SourceType.HTML
            }
            val source = SourceDefinition(
                id = "custom-" + displayName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
                displayName = displayName.trim(),
                websiteUrl = normalizedUrl,
                description = description.trim().ifBlank { "User-added source" },
                supportsAutomatedSync = true,
                isUserAdded = true,
                sourceType = inferredType,
            )
            current.removeAll { it.id == source.id }
            current.add(source)
            it[Keys.CustomSources] = json.encodeToString(current)
            val enabled = it[Keys.EnabledSourceIds]?.let(::decodeEnabledIds).orEmpty() + source.id
            it[Keys.EnabledSourceIds] = encodeEnabledIds(enabled)
            val order = normalizeSourceOrder(it[Keys.SourceOrder], current).toMutableList()
            if (source.id !in order) {
                order.add(source.id)
                it[Keys.SourceOrder] = json.encodeToString(order)
            }
        }
    }

    suspend fun removeCustomSource(sourceId: String) {
        dataStore.edit {
            val current = decodeCustomSources(it[Keys.CustomSources]).filterNot { source -> source.id == sourceId }
            it[Keys.CustomSources] = json.encodeToString(current)
            val enabled = it[Keys.EnabledSourceIds]?.let(::decodeEnabledIds).orEmpty() - sourceId
            it[Keys.EnabledSourceIds] = encodeEnabledIds(enabled)
            val order = normalizeSourceOrder(it[Keys.SourceOrder], current).filterNot { it == sourceId }
            it[Keys.SourceOrder] = json.encodeToString(order)
        }
    }

    suspend fun replaceAllFromBackup(backupJson: String): Result<Unit> = runCatching {
        val payload = json.decodeFromString<BackupPayload>(backupJson)
        dataStore.edit {
            it[Keys.BackgroundSyncEnabled] = payload.backgroundSyncEnabled
            it[Keys.RemoteSourceEnabled] = payload.remoteSourceEnabled
            it[Keys.AppLanguage] = payload.appLanguage.tag
            it[Keys.ThemeMode] = payload.themeMode.storageValue
            it[Keys.SyncInterval] = payload.syncInterval.storageValue
            it[Keys.CustomSources] = json.encodeToString(payload.customSources)
            it[Keys.EnabledSourceIds] = encodeEnabledIds(payload.enabledSourceIds)
            it[Keys.SourceOrder] = json.encodeToString(payload.sourceOrder)
            it[Keys.SavedSearches] = json.encodeToString(payload.savedSearches)
        }
    }

    suspend fun exportBackupJson(): String {
        val settings = appSettings.first()
        return json.encodeToString(
            BackupPayload(
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
        )
    }

    suspend fun saveSearch(search: SavedSearch) {
        dataStore.edit {
            val current = decodeSavedSearches(it[Keys.SavedSearches]).toMutableList()
            current.removeAll { saved -> saved.id == search.id }
            current.add(0, search)
            it[Keys.SavedSearches] = json.encodeToString(current)
        }
    }

    suspend fun deleteSavedSearch(searchId: String) {
        dataStore.edit {
            val current = decodeSavedSearches(it[Keys.SavedSearches]).filterNot { search -> search.id == searchId }
            it[Keys.SavedSearches] = json.encodeToString(current)
        }
    }

    suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean) {
        dataStore.edit {
            val current = decodeSavedSearches(it[Keys.SavedSearches]).map { search ->
                if (search.id == searchId) search.copy(alertsEnabled = enabled) else search
            }
            it[Keys.SavedSearches] = json.encodeToString(current)
        }
    }


    suspend fun getSeenAlertMatches(): Map<String, Set<Long>> {
        val raw = dataStore.data.first()[Keys.SeenAlertMatches]
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, Set<Long>>>(raw) }.getOrDefault(emptyMap())
    }

    suspend fun markSeenAlertMatches(matches: Map<String, Set<Long>>) {
        dataStore.edit { prefs ->
            prefs[Keys.SeenAlertMatches] = json.encodeToString(matches)
        }
    }

    private fun defaultEnabledSourceIds(remoteEnabled: Boolean): Set<String> {
        val defaults = SourceCatalog.all.filter { it.supportsAutomatedSync }.map { it.id }.toMutableSet()
        if (!remoteEnabled) defaults.remove(SourceCatalog.RemoteJson.id)
        return defaults
    }

    private fun decodeEnabledIds(raw: String): Set<String> =
        runCatching { json.decodeFromString<List<String>>(raw).toSet() }.getOrDefault(emptySet())

    private fun encodeEnabledIds(ids: Set<String>): String = json.encodeToString(ids.sorted())

    private fun decodeCustomSources(raw: String?): List<SourceDefinition> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<SourceDefinition>>(raw) }.getOrDefault(emptyList())
    }

    private fun decodeSavedSearches(raw: String?): List<SavedSearch> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<SavedSearch>>(raw) }.getOrDefault(emptyList())
    }

    private fun normalizeSourceOrder(rawOrder: String?, customSources: List<SourceDefinition>): List<String> {
        val baseOrder = runCatching { json.decodeFromString<List<String>>(rawOrder ?: "") }.getOrNull().orEmpty()
        val known = (SourceCatalog.all + customSources).map { source -> source.id }.distinct()
        val normalized = baseOrder.filter { it in known }.toMutableList()
        known.forEach { id ->
            if (id !in normalized) normalized.add(id)
        }
        return normalized
    }

    @Serializable
    private data class BackupPayload(
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
}
