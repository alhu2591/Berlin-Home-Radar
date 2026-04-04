@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.presentation.util.formatTimestamp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsScreen(
    uiState: StateFlow<SettingsUiState>,
    snackbarHostState: SnackbarHostState,
    onBackgroundSyncChanged: (Boolean) -> Unit,
    onRemoteSourceChanged: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onSyncIntervalSelected: (SyncIntervalOption) -> Unit,
    onManageSources: () -> Unit,
    onManualRefresh: () -> Unit,
    onRefreshRemoteConfig: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        SettingsContent(
            state = state,
            padding = padding,
            onBackgroundSyncChanged = onBackgroundSyncChanged,
            onRemoteSourceChanged = onRemoteSourceChanged,
            onLanguageSelected = onLanguageSelected,
            onThemeSelected = onThemeSelected,
            onSyncIntervalSelected = onSyncIntervalSelected,
            onManageSources = onManageSources,
            onManualRefresh = onManualRefresh,
            onRefreshRemoteConfig = onRefreshRemoteConfig,
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    padding: PaddingValues,
    onBackgroundSyncChanged: (Boolean) -> Unit,
    onRemoteSourceChanged: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onSyncIntervalSelected: (SyncIntervalOption) -> Unit,
    onManageSources: () -> Unit,
    onManualRefresh: () -> Unit,
    onRefreshRemoteConfig: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        val wideLayout = maxWidth >= 900.dp
        if (wideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ChoiceSection(
                        title = stringResource(R.string.settings_language_title),
                        subtitle = stringResource(R.string.settings_language_subtitle),
                    ) {
                        ChoiceChips(
                            options = AppLanguage.entries.toList(),
                            selected = state.language,
                            onSelected = onLanguageSelected,
                            label = { option -> Text(languageLabel(option)) },
                        )
                    }
                    ChoiceSection(
                        title = stringResource(R.string.settings_theme_title),
                        subtitle = stringResource(R.string.settings_theme_subtitle),
                    ) {
                        ChoiceChips(
                            options = ThemeMode.entries.toList(),
                            selected = state.themeMode,
                            onSelected = onThemeSelected,
                            label = { option -> Text(themeLabel(option)) },
                        )
                    }
                    ChoiceSection(
                        title = stringResource(R.string.settings_sync_controls),
                        subtitle = stringResource(R.string.settings_sync_explanation),
                    ) {
                        ChoiceChips(
                            options = SyncIntervalOption.entries.toList(),
                            selected = state.syncInterval,
                            onSelected = onSyncIntervalSelected,
                            label = { option -> Text(intervalLabel(option)) },
                        )
                    }
                    ToggleSettingsCard(
                        backgroundSyncEnabled = state.backgroundSyncEnabled,
                        remoteSourceEnabled = state.remoteSourceEnabled,
                        onBackgroundSyncChanged = onBackgroundSyncChanged,
                        onRemoteSourceChanged = onRemoteSourceChanged,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeroCard(
                        syncInterval = intervalLabel(state.syncInterval),
                        lastSuccessfulSyncText = formatTimestamp(state.lastSuccessfulSyncMillis),
                        sourcesCount = state.sources.size,
                        onManualRefresh = onManualRefresh,
                    )
                    DiagnosticsSection(
                        lastSuccessfulSyncText = formatTimestamp(state.lastSuccessfulSyncMillis),
                        lastAttemptText = formatTimestamp(state.lastAttemptMillis),
                        lastErrorMessage = state.lastErrorMessage,
                    )
                    RemoteConfigSection(
                        state = state,
                        onRefreshRemoteConfig = onRefreshRemoteConfig,
                    )
                    SourceManagementSection(onManageSources = onManageSources)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HeroCard(
                    syncInterval = intervalLabel(state.syncInterval),
                    lastSuccessfulSyncText = formatTimestamp(state.lastSuccessfulSyncMillis),
                    sourcesCount = state.sources.size,
                    onManualRefresh = onManualRefresh,
                )
                ChoiceSection(
                    title = stringResource(R.string.settings_language_title),
                    subtitle = stringResource(R.string.settings_language_subtitle),
                ) {
                    ChoiceChips(
                        options = AppLanguage.entries.toList(),
                        selected = state.language,
                        onSelected = onLanguageSelected,
                        label = { option -> Text(languageLabel(option)) },
                    )
                }
                ChoiceSection(
                    title = stringResource(R.string.settings_theme_title),
                    subtitle = stringResource(R.string.settings_theme_subtitle),
                ) {
                    ChoiceChips(
                        options = ThemeMode.entries.toList(),
                        selected = state.themeMode,
                        onSelected = onThemeSelected,
                        label = { option -> Text(themeLabel(option)) },
                    )
                }
                ChoiceSection(
                    title = stringResource(R.string.settings_sync_controls),
                    subtitle = stringResource(R.string.settings_sync_explanation),
                ) {
                    ChoiceChips(
                        options = SyncIntervalOption.entries.toList(),
                        selected = state.syncInterval,
                        onSelected = onSyncIntervalSelected,
                        label = { option -> Text(intervalLabel(option)) },
                    )
                }
                ToggleSettingsCard(
                    backgroundSyncEnabled = state.backgroundSyncEnabled,
                    remoteSourceEnabled = state.remoteSourceEnabled,
                    onBackgroundSyncChanged = onBackgroundSyncChanged,
                    onRemoteSourceChanged = onRemoteSourceChanged,
                )
                DiagnosticsSection(
                    lastSuccessfulSyncText = formatTimestamp(state.lastSuccessfulSyncMillis),
                    lastAttemptText = formatTimestamp(state.lastAttemptMillis),
                    lastErrorMessage = state.lastErrorMessage,
                )
                RemoteConfigSection(
                    state = state,
                    onRefreshRemoteConfig = onRefreshRemoteConfig,
                )
                SourceManagementSection(onManageSources = onManageSources)
            }
        }
    }
}

