package com.berlin.homeradar.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.berlin.homeradar.presentation.util.formatTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSyncInfoUseCase: ObserveSyncInfoUseCase,
    private val updateSyncSettingsUseCase: UpdateSyncSettingsUseCase,
    private val syncScheduler: SyncScheduler,
    private val appSettingsApplier: AppSettingsApplier,
    private val sourceHealthMonitor: SourceHealthMonitor,
    repository: HousingRepository,
) : ViewModel() {

    private val builtInSources = repository.getKnownSources()

    val uiState = combine(observeSyncInfoUseCase(), sourceHealthMonitor.statuses) { info, statuses ->
        SettingsUiState(
            backgroundSyncEnabled = info.backgroundSyncEnabled,
            remoteSourceEnabled = info.remoteSourceEnabled,
            language = info.appLanguage,
            themeMode = info.themeMode,
            syncInterval = info.syncInterval,
            lastSuccessfulSyncText = formatTimestamp(info.lastSuccessfulSyncMillis),
            lastAttemptText = formatTimestamp(info.lastAttemptMillis),
            lastErrorMessage = info.lastErrorMessage,
            sources = mergeSources(
                builtInSources = builtInSources,
                customSources = info.customSources,
                sourceOrder = info.sourceOrder,
            ),
            enabledSourceIds = info.enabledSourceIds,
            sourceHealth = statuses,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(sources = builtInSources),
    )

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
            onReady(updateSyncSettingsUseCase.exportBackupJson())
        }
    }

    fun importBackup(json: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = updateSyncSettingsUseCase.importBackupJson(json)
            result.onSuccess {
                val state = uiState.value
                appSettingsApplier.applyLanguage(state.language)
                appSettingsApplier.applyTheme(state.themeMode)
                if (state.backgroundSyncEnabled) {
                    syncScheduler.schedulePeriodicSync(state.syncInterval)
                } else {
                    syncScheduler.cancelPeriodicSync()
                }
            }
            onDone(result.isSuccess)
        }
    }

    fun manualRefresh() {
        syncScheduler.manualRefresh()
    }

    fun testSource(sourceId: String) {
        viewModelScope.launch {
            val source = uiState.value.sources.firstOrNull { it.id == sourceId } ?: return@launch
            sourceHealthMonitor.testSource(source)
        }
    }

    private fun mergeSources(
        builtInSources: List<SourceDefinition>,
        customSources: List<SourceDefinition>,
        sourceOrder: List<String>,
    ): List<SourceDefinition> {
        val all = (builtInSources + customSources).associateBy { it.id }
        return (sourceOrder + all.keys).distinct().mapNotNull { all[it] }
    }
}
