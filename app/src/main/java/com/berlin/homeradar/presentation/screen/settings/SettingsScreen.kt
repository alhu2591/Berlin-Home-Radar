package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: StateFlow<SettingsUiState>,
    onBackgroundSyncChanged: (Boolean) -> Unit,
    onRemoteSourceChanged: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onSyncIntervalSelected: (SyncIntervalOption) -> Unit,
    onManageSources: () -> Unit,
    onManualRefresh: () -> Unit,
) {
    val state by uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.settings_sync_controls), style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(R.string.settings_sync_explanation), style = MaterialTheme.typography.bodyMedium)
                        ChoiceChips(
                            options = SyncIntervalOption.entries.toList(),
                            selected = state.syncInterval,
                            onSelected = onSyncIntervalSelected,
                            label = { option -> Text(intervalLabel(option)) },
                        )
                        Button(onClick = onManualRefresh) {
                            Text(stringResource(R.string.settings_manual_refresh))
                        }
                    }
                }
            }
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleMedium)
                        ChoiceChips(
                            options = AppLanguage.entries.toList(),
                            selected = state.language,
                            onSelected = onLanguageSelected,
                            label = { option -> Text(languageLabel(option)) },
                        )
                        Text(text = stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
                        ChoiceChips(
                            options = ThemeMode.entries.toList(),
                            selected = state.themeMode,
                            onSelected = onThemeSelected,
                            label = { option -> Text(themeLabel(option)) },
                        )
                    }
                }
            }
            item {
                Card {
                    Column {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_background_sync)) },
                            supportingContent = { Text(stringResource(R.string.settings_background_sync_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = state.backgroundSyncEnabled,
                                    onCheckedChange = onBackgroundSyncChanged,
                                )
                            }
                        )
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.settings_remote_source)) },
                            supportingContent = { Text(stringResource(R.string.settings_remote_source_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = state.remoteSourceEnabled,
                                    onCheckedChange = onRemoteSourceChanged,
                                )
                            }
                        )
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(stringResource(R.string.settings_last_successful_sync, state.lastSuccessfulSyncText))
                        Text(stringResource(R.string.settings_last_attempt, state.lastAttemptText))
                        Text(stringResource(R.string.settings_last_error, state.lastErrorMessage ?: stringResource(R.string.none_label)))
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.settings_sources_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.settings_sources_subtitle), style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = onManageSources) {
                            Text(stringResource(R.string.settings_manage_sources_button))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.GERMAN -> stringResource(R.string.language_german)
    AppLanguage.ARABIC -> stringResource(R.string.language_arabic)
}

@Composable
private fun themeLabel(themeMode: ThemeMode): String = when (themeMode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
private fun intervalLabel(option: SyncIntervalOption): String = when (option) {
    SyncIntervalOption.MANUAL -> stringResource(R.string.sync_manual)
    SyncIntervalOption.MINUTES_15 -> stringResource(R.string.sync_15m)
    SyncIntervalOption.MINUTES_30 -> stringResource(R.string.sync_30m)
    SyncIntervalOption.HOUR_1 -> stringResource(R.string.sync_1h)
    SyncIntervalOption.HOURS_3 -> stringResource(R.string.sync_3h)
}

@Composable
private fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: @Composable (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { label(option) },
            )
        }
    }
}
