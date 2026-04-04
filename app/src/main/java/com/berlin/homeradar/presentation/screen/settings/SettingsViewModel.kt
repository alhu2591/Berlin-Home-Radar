package com.berlin.homeradar.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.R
import com.berlin.homeradar.data.config.RemoteConfigManager
import com.berlin.homeradar.data.preferences.AppSettingsApplier
import com.berlin.homeradar.data.source.SourceHealthMonitor
import com.berlin.homeradar.data.sync.SyncScheduler
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import com.berlin.homeradar.domain.usecase.ObserveSyncInfoUseCase
import com.berlin.homeradar.domain.usecase.UpdateSyncSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSyncInfoUseCase: ObserveSyncInfoUseCase,
    private val updateSyncSettingsUseCase: UpdateSyncSettingsUseCase,
    private val syncScheduler: SyncScheduler,
    private val appSettingsApplier: AppSettingsApplier,
    private val sourceHealthMonitor: SourceHealthMonitor,
    private val remoteConfigManager: RemoteConfigManager,
    private val repository: HousingRepository,
) : ViewModel() {

    private val builtInSources = repository.getKnownSources()
    private val isImporting = MutableStateFlow(false)
    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    val uiState = combine(
        observeSyncInfoUseCase(),
        sourceHealthMonitor.statuses,
        repository.observeSourceReliabilityMetrics(),
        remoteConfigManager.info,
        isImporting,
    ) { info, statuses, sourceMetrics, remoteConfigInfo, importing ->
        SettingsUiState(
            backgroundSyncEnabled = info.backgroundSyncEnabled,
            remoteSourceEnabled = info.remoteSourceEnabled,
            language = info.appLanguage,
            themeMode = info.themeMode,
            syncInterval = info.syncInterval,
            lastSuccessfulSyncMillis = info.lastSuccessfulSyncMillis,
            lastAttemptMillis = info.lastAttemptMillis,
            lastErrorMessage = info.lastErrorMessage,
            sources = mergeSources(
                builtInSources = builtInSources,
                customSources = info.customSources,
                sourceOrder = info.sourceOrder,
            ),
            enabledSourceIds = info.enabledSourceIds,
            sourceHealth = statuses,
            sourceMetrics = sourceMetrics,
            remoteConfigInfo = remoteConfigInfo,
            isImporting = importing,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(sources = builtInSources),
    )

    init {
        viewModelScope.launch {
            remoteConfigManager.refreshIfStale()
        }
    }

    fun onBackgroundSyncChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setBackgroundSyncEnabled(enabled)
            if (enabled) syncScheduler.schedulePeriodicSync(uiState.value.syncInterval) else syncScheduler.cancelPeriodicSync()
        }
    }

    fun onRemoteSourceChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setRemoteSourceEnabled(enabled)
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setAppLanguage(language)
            appSettingsApplier.applyLanguage(language)
        }
    }

    fun onThemeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setThemeMode(themeMode)
            appSettingsApplier.applyTheme(themeMode)
        }
    }

    fun onSyncIntervalSelected(option: SyncIntervalOption) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setSyncInterval(option)
            if (uiState.value.backgroundSyncEnabled) {
                syncScheduler.schedulePeriodicSync(option)
            } else {
                syncScheduler.cancelPeriodicSync()
            }
        }
    }

    fun onSourceEnabledChanged(sourceId: String, enabled: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setSourceEnabled(sourceId, enabled)
        }
    }

    fun enableAllSupportedSources() {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setAllSupportedSourcesEnabled(true)
        }
    }

    fun disableAllSupportedSources() {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setAllSupportedSourcesEnabled(false)
        }
    }

    fun moveSource(sourceId: String, moveUp: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.moveSource(sourceId, moveUp)
        }
    }

    fun addCustomSource(displayName: String, websiteUrl: String, description: String) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.addCustomSource(
                displayName = displayName,
                websiteUrl = websiteUrl,
                description = description,
            )
        }
    }

    fun removeCustomSource(sourceId: String) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.removeCustomSource(sourceId)
        }
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { updateSyncSettingsUseCase.exportBackupJson() }
                .onSuccess(onReady)
                .onFailure { _messages.emit(UiMessage(R.string.backup_export_failed)) }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            if (isImporting.value) return@launch

            val trimmed = json.trim()
            when {
                trimmed.isBlank() -> {
                    _messages.emit(UiMessage(R.string.backup_import_invalid_empty))
                    return@launch
                }
                !isLikelyBackupJson(trimmed) -> {
                    _messages.emit(UiMessage(R.string.backup_import_invalid_format))
                    return@launch
                }
            }

            isImporting.value = true
            val result = updateSyncSettingsUseCase.importBackupJson(trimmed)
            result.onSuccess {
                val info = repository.observeSyncInfo().first()
                appSettingsApplier.applyLanguage(info.appLanguage)
                appSettingsApplier.applyTheme(info.themeMode)
                if (info.backgroundSyncEnabled) {
                    syncScheduler.schedulePeriodicSync(info.syncInterval)
                } else {
                    syncScheduler.cancelPeriodicSync()
                }
                _messages.emit(UiMessage(R.string.backup_import_success))
            }.onFailure {
                _messages.emit(UiMessage(R.string.backup_import_failed))
            }
            isImporting.value = false
        }
    }

    fun manualRefresh() {
        syncScheduler.manualRefresh()
        viewModelScope.launch {
            _messages.emit(UiMessage(R.string.manual_refresh_started))
        }
    }

    fun refreshRemoteConfig() {
        viewModelScope.launch {
            remoteConfigManager.refresh()
                .onSuccess { _messages.emit(UiMessage(R.string.remote_config_refresh_success)) }
                .onFailure { _messages.emit(UiMessage(R.string.remote_config_refresh_failed)) }
        }
    }

    fun testSource(sourceId: String) {
        viewModelScope.launch {
            val source = uiState.value.sources.firstOrNull { it.id == sourceId } ?: return@launch
            val result = sourceHealthMonitor.testSource(source)
            when {
                !source.supportsAutomatedSync && !source.isUserAdded -> {
                    _messages.emit(UiMessage(R.string.source_test_manual_only, listOf(source.displayName)))
                }
                result.isSuccess -> {
                    _messages.emit(UiMessage(R.string.source_test_success, listOf(source.displayName)))
                }
                else -> {
                    _messages.emit(UiMessage(R.string.source_test_failed, listOf(source.displayName)))
                }
            }
        }
    }

    private fun isLikelyBackupJson(raw: String): Boolean {
        val jsonObject = runCatching { Json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: return false
        return REQUIRED_BACKUP_KEYS.all { key -> key in jsonObject }
    }

    private fun mergeSources(
        builtInSources: List<SourceDefinition>,
        customSources: List<SourceDefinition>,
        sourceOrder: List<String>,
    ): List<SourceDefinition> {
        val all = (builtInSources + customSources).associateBy { it.id }
        return (sourceOrder + all.keys).distinct().mapNotNull { all[it] }
    }

    companion object {
        private val REQUIRED_BACKUP_KEYS = setOf(
            "backgroundSyncEnabled",
            "remoteSourceEnabled",
            "appLanguage",
            "themeMode",
            "syncInterval",
            "enabledSourceIds",
            "customSources",
            "sourceOrder",
        )
    }
}
