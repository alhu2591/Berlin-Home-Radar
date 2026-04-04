package com.berlin.homeradar.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class UserPreferencesRepository private constructor(
    private val dataStore: DataStore<Preferences>,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(createDataStore(context))

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val storage = UserPreferencesStorage(json)

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
        val customSources = storage.decodeCustomSources(prefs[Keys.CustomSources])

        AppSettings(
            language = AppLanguage.fromTag(prefs[Keys.AppLanguage]),
            themeMode = ThemeMode.fromStorage(prefs[Keys.ThemeMode]),
            syncInterval = SyncIntervalOption.fromStorage(prefs[Keys.SyncInterval]),
            backgroundSyncEnabled = prefs[Keys.BackgroundSyncEnabled] ?: true,
            remoteSourceEnabled = remoteEnabled,
            enabledSourceIds = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds)
                ?: storage.defaultEnabledSourceIds(remoteEnabled),
            customSources = customSources,
            sourceOrder = storage.normalizeSourceOrder(
                rawOrder = prefs[Keys.SourceOrder],
                customSources = customSources,
            ),
            savedSearches = storage.decodeSavedSearches(prefs[Keys.SavedSearches]),
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
        dataStore.edit { prefs ->
            prefs[Keys.RemoteSourceEnabled] = enabled
            val current = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds)
                ?: storage.defaultEnabledSourceIds(!enabled)
            val updated = if (enabled) current + SourceCatalog.RemoteJson.id else current - SourceCatalog.RemoteJson.id
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(updated)
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
        updateEnabledSourceIds { current -> if (enabled) current + sourceId else current - sourceId }
    }

    suspend fun setAllSupportedSourcesEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds) ?: emptySet()
            val customSources = storage.decodeCustomSources(prefs[Keys.CustomSources])
            val customSyncIds = customSources
                .asSequence()
                .filter { it.supportsAutomatedSync }
                .map { it.id }
                .toSet()
            val target = SourceCatalog.supportedSyncSourceIds + customSyncIds
            val updated = if (enabled) current + target else current - target
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(updated)
        }
    }

    suspend fun moveSource(sourceId: String, moveUp: Boolean) {
        dataStore.edit { prefs ->
            val customSources = storage.decodeCustomSources(prefs[Keys.CustomSources])
            val order = storage.normalizeSourceOrder(prefs[Keys.SourceOrder], customSources).toMutableList()
            val index = order.indexOf(sourceId)
            if (index == -1) return@edit

            val swapIndex = if (moveUp) index - 1 else index + 1
            if (swapIndex !in order.indices) return@edit

            order[index] = order[swapIndex].also { order[swapIndex] = order[index] }
            prefs[Keys.SourceOrder] = json.encodeToString(order)
        }
    }

    suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String) {
        dataStore.edit { prefs ->
            val current = storage.decodeCustomSources(prefs[Keys.CustomSources]).toMutableList()
            val source = storage.buildCustomSource(displayName, websiteUrl, description)

            current.removeAll { it.id == source.id }
            current.add(source)
            prefs[Keys.CustomSources] = json.encodeToString(current)

            val enabled = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds).orEmpty() + source.id
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(enabled)

            val order = storage.normalizeSourceOrder(prefs[Keys.SourceOrder], current).toMutableList()
            if (source.id !in order) {
                order.add(source.id)
                prefs[Keys.SourceOrder] = json.encodeToString(order)
            }
        }
    }

    suspend fun removeCustomSource(sourceId: String) {
        dataStore.edit { prefs ->
            val current = storage.decodeCustomSources(prefs[Keys.CustomSources]).filterNot { it.id == sourceId }
            prefs[Keys.CustomSources] = json.encodeToString(current)

            val enabled = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds).orEmpty() - sourceId
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(enabled)

            val order = storage.normalizeSourceOrder(prefs[Keys.SourceOrder], current).filterNot { it == sourceId }
            prefs[Keys.SourceOrder] = json.encodeToString(order)
        }
    }

    suspend fun replaceAllFromBackup(backupJson: String): Result<Unit> = runCatching {
        val payload = storage.normalizeBackupPayload(
            json.decodeFromString<UserPreferencesStorage.BackupPayload>(backupJson),
        )
        dataStore.edit { prefs ->
            prefs[Keys.BackgroundSyncEnabled] = payload.backgroundSyncEnabled
            prefs[Keys.RemoteSourceEnabled] = payload.remoteSourceEnabled
            prefs[Keys.AppLanguage] = payload.appLanguage.tag
            prefs[Keys.ThemeMode] = payload.themeMode.storageValue
            prefs[Keys.SyncInterval] = payload.syncInterval.storageValue
            prefs[Keys.CustomSources] = json.encodeToString(payload.customSources)
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(payload.enabledSourceIds)
            prefs[Keys.SourceOrder] = json.encodeToString(payload.sourceOrder)
            prefs[Keys.SavedSearches] = json.encodeToString(payload.savedSearches)
        }
    }

    suspend fun exportBackupJson(): String {
        val settings = appSettings.first()
        return json.encodeToString(storage.backupPayloadFromSettings(settings))
    }

    suspend fun saveSearch(search: SavedSearch) {
        updateSavedSearches { current ->
            current.toMutableList().apply {
                removeAll { it.id == search.id }
                add(0, search)
            }
        }
    }

    suspend fun deleteSavedSearch(searchId: String) {
        updateSavedSearches { current -> current.filterNot { it.id == searchId } }
    }

    suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean) {
        updateSavedSearches { current ->
            current.map { if (it.id == searchId) it.copy(alertsEnabled = enabled) else it }
        }
    }

    suspend fun getSeenAlertMatches(): Map<String, Set<Long>> {
        val raw = dataStore.data.first()[Keys.SeenAlertMatches]
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, Set<Long>>>(raw) }.getOrDefault(emptyMap())
    }

    suspend fun markSeenAlertMatches(matches: Map<String, Set<Long>>) {
        dataStore.edit { prefs -> prefs[Keys.SeenAlertMatches] = json.encodeToString(matches) }
    }

    private suspend fun updateEnabledSourceIds(transform: (Set<String>) -> Set<String>) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.EnabledSourceIds]?.let(storage::decodeEnabledIds).orEmpty()
            prefs[Keys.EnabledSourceIds] = storage.encodeEnabledIds(transform(current))
        }
    }

    private suspend fun updateSavedSearches(transform: (List<SavedSearch>) -> List<SavedSearch>) {
        dataStore.edit { prefs ->
            val current = storage.decodeSavedSearches(prefs[Keys.SavedSearches])
            prefs[Keys.SavedSearches] = json.encodeToString(transform(current))
        }
    }

    companion object {
        private const val DEFAULT_DATASTORE_FILE_NAME = "user_prefs.preferences_pb"

        private fun createDataStore(
            context: Context,
            fileName: String = DEFAULT_DATASTORE_FILE_NAME,
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(fileName) },
        )

        internal fun createForTesting(
            context: Context,
            fileName: String,
        ): UserPreferencesRepository = UserPreferencesRepository(createDataStore(context, fileName))
    }
}
