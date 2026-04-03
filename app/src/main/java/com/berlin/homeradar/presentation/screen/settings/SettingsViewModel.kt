package com.berlin.homeradar.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.data.sync.SyncScheduler
import com.berlin.homeradar.domain.usecase.ObserveSyncInfoUseCase
import com.berlin.homeradar.domain.usecase.UpdateSyncSettingsUseCase
import com.berlin.homeradar.presentation.util.formatTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSyncInfoUseCase: ObserveSyncInfoUseCase,
    private val updateSyncSettingsUseCase: UpdateSyncSettingsUseCase,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    val uiState = observeSyncInfoUseCase().map { info ->
        SettingsUiState(
            backgroundSyncEnabled = info.backgroundSyncEnabled,
            remoteSourceEnabled = info.remoteSourceEnabled,
            lastSuccessfulSyncText = formatTimestamp(info.lastSuccessfulSyncMillis),
            lastAttemptText = formatTimestamp(info.lastAttemptMillis),
            lastErrorMessage = info.lastErrorMessage,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(),
    )

    fun onBackgroundSyncChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setBackgroundSyncEnabled(enabled)
            if (enabled) syncScheduler.schedulePeriodicSync() else syncScheduler.cancelPeriodicSync()
        }
    }

    fun onRemoteSourceChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateSyncSettingsUseCase.setRemoteSourceEnabled(enabled)
        }
    }

    fun manualRefresh() {
        syncScheduler.manualRefresh()
    }
}
