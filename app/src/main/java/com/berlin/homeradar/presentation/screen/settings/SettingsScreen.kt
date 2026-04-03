@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroCard(
                    syncInterval = intervalLabel(state.syncInterval),
                    lastSuccessfulSyncText = state.lastSuccessfulSyncText,
                    sourcesCount = state.sources.size,
                    onManualRefresh = onManualRefresh,
                )
            }

            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_language_title),
                    subtitle = "Choose the app language or keep the system default.",
                ) {
                    ChoiceChips(
                        options = AppLanguage.entries.toList(),
                        selected = state.language,
                        onSelected = onLanguageSelected,
                        label = { option -> Text(languageLabel(option)) },
                    )
                }
            }

            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_theme_title),
                    subtitle = "Light, dark, or follow the device automatically.",
                ) {
                    ChoiceChips(
                        options = ThemeMode.entries.toList(),
                        selected = state.themeMode,
                        onSelected = onThemeSelected,
                        label = { option -> Text(themeLabel(option)) },
                    )
                }
            }

            item {
                SettingsSectionCard(
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
                SettingsSectionCard(
                    title = "Sync diagnostics",
                    subtitle = "These values help you confirm that scheduled work and source retries are functioning correctly.",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.settings_last_successful_sync, state.lastSuccessfulSyncText))
                        Text(stringResource(R.string.settings_last_attempt, state.lastAttemptText))
                        Text(stringResource(R.string.settings_last_error, state.lastErrorMessage ?: stringResource(R.string.none_label)))
                    }
                }
            }

            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_sources_title),
                    subtitle = "Enable, disable, reorder, test, add, or remove custom sources from one place.",
                ) {
                    OutlinedButton(onClick = onManageSources) {
                        Text(stringResource(R.string.settings_manage_sources_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    syncInterval: String,
    lastSuccessfulSyncText: String,
    sourcesCount: Int,
    onManualRefresh: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("App overview", style = MaterialTheme.typography.titleLarge)
            Text(
                "The app keeps the latest listings locally and refreshes them in the background using WorkManager.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Interval: $syncInterval • Sources: $sourcesCount • Last sync: $lastSuccessfulSyncText",
                style = MaterialTheme.typography.labelLarge,
            )
            Button(onClick = onManualRefresh) {
                Text(stringResource(R.string.settings_manual_refresh))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            content()
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
    SyncIntervalOption.MANUAL -> stringResource(R.string.interval_manual)
    SyncIntervalOption.MINUTES_15 -> stringResource(R.string.interval_15m)
    SyncIntervalOption.MINUTES_30 -> stringResource(R.string.interval_30m)
    SyncIntervalOption.HOUR_1 -> stringResource(R.string.interval_1h)
    SyncIntervalOption.HOURS_3 -> stringResource(R.string.interval_3h)
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
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { label(option) },
            )
        }
    }
}
