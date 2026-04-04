package com.berlin.homeradar.presentation.screen.settings

import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.RemoteConfigInfo
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SourceHealth
import com.berlin.homeradar.domain.model.SourceReliabilityMetrics
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode

data class SettingsUiState(
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val syncInterval: SyncIntervalOption = SyncIntervalOption.MINUTES_15,
    val lastSuccessfulSyncMillis: Long? = null,
    val lastAttemptMillis: Long? = null,
    val lastErrorMessage: String? = null,
    val sources: List<SourceDefinition> = emptyList(),
    val enabledSourceIds: Set<String> = emptySet(),
    val sourceHealth: Map<String, SourceHealth> = emptyMap(),
    val sourceMetrics: Map<String, SourceReliabilityMetrics> = emptyMap(),
    val remoteConfigInfo: RemoteConfigInfo = RemoteConfigInfo(),
    val isImporting: Boolean = false,
)
