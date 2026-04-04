package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.berlin.homeradar.R
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.presentation.util.formatTimestamp

@Composable
internal fun HeroCard(
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
            Text(
                text = stringResource(R.string.settings_app_overview_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.settings_app_overview_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.settings_app_overview_meta,
                    syncInterval,
                    sourcesCount,
                    lastSuccessfulSyncText,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.adaptive_filters_title),
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onManualRefresh) {
                Text(stringResource(R.string.settings_manual_refresh))
            }
        }
    }
}

@Composable
internal fun ChoiceSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    SettingsSectionCard(
        title = title,
        subtitle = subtitle,
        content = content,
    )
}

@Composable
internal fun ToggleSettingsCard(
    backgroundSyncEnabled: Boolean,
    remoteSourceEnabled: Boolean,
    onBackgroundSyncChanged: (Boolean) -> Unit,
    onRemoteSourceChanged: (Boolean) -> Unit,
) {
    Card {
        Column {
            SettingToggleItem(
                title = stringResource(R.string.settings_background_sync),
                subtitle = stringResource(R.string.settings_background_sync_desc),
                checked = backgroundSyncEnabled,
                onCheckedChange = onBackgroundSyncChanged,
            )
            SettingToggleItem(
                title = stringResource(R.string.settings_remote_source),
                subtitle = stringResource(R.string.settings_remote_source_desc),
                checked = remoteSourceEnabled,
                onCheckedChange = onRemoteSourceChanged,
            )
        }
    }
}

@Composable
internal fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
internal fun DiagnosticsSection(
    lastSuccessfulSyncText: String,
    lastAttemptText: String,
    lastErrorMessage: String?,
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_diagnostics_title),
        subtitle = stringResource(R.string.settings_diagnostics_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.settings_last_successful_sync, lastSuccessfulSyncText))
            Text(stringResource(R.string.settings_last_attempt, lastAttemptText))
            Text(
                stringResource(
                    R.string.settings_last_error,
                    lastErrorMessage ?: stringResource(R.string.none_label),
                ),
            )
        }
    }
}

@Composable
internal fun RemoteConfigSection(
    state: SettingsUiState,
    onRefreshRemoteConfig: () -> Unit,
) {
    SettingsSectionCard(
        title = stringResource(R.string.remote_config_title),
        subtitle = stringResource(R.string.remote_config_subtitle),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (state.remoteConfigInfo.isEnabled) {
                    stringResource(R.string.remote_config_status_enabled)
                } else {
                    stringResource(R.string.remote_config_status_disabled)
                },
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(
                    R.string.remote_config_endpoint,
                    state.remoteConfigInfo.endpointUrl ?: stringResource(R.string.none_label),
                ),
            )
            Text(
                text = stringResource(
                    R.string.remote_config_last_fetch,
                    formatTimestamp(state.remoteConfigInfo.lastSuccessfulFetchMillis),
                ),
            )
            if (!state.remoteConfigInfo.lastErrorMessage.isNullOrBlank()) {
                Text(
                    text = stringResource(
                        R.string.remote_config_last_error,
                        state.remoteConfigInfo.lastErrorMessage.orEmpty(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(
                onClick = onRefreshRemoteConfig,
                enabled = state.remoteConfigInfo.isEnabled,
                modifier = Modifier.widthIn(min = 220.dp),
            ) {
                Text(stringResource(R.string.remote_config_refresh))
            }
        }
    }
}

@Composable
internal fun SourceManagementSection(
    onManageSources: () -> Unit,
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_sources_title),
        subtitle = stringResource(R.string.settings_sources_subtitle),
    ) {
        OutlinedButton(onClick = onManageSources) {
            Text(stringResource(R.string.settings_manage_sources_button))
        }
    }
}

@Composable
internal fun SettingsSectionCard(
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
internal fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.GERMAN -> stringResource(R.string.language_german)
    AppLanguage.ARABIC -> stringResource(R.string.language_arabic)
}

@Composable
internal fun themeLabel(themeMode: ThemeMode): String = when (themeMode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
internal fun intervalLabel(option: SyncIntervalOption): String = when (option) {
    SyncIntervalOption.MANUAL -> stringResource(R.string.sync_manual)
    SyncIntervalOption.MINUTES_15 -> stringResource(R.string.sync_15m)
    SyncIntervalOption.MINUTES_30 -> stringResource(R.string.sync_30m)
    SyncIntervalOption.HOUR_1 -> stringResource(R.string.sync_1h)
    SyncIntervalOption.HOURS_3 -> stringResource(R.string.sync_3h)
}

@Composable
internal fun <T> ChoiceChips(
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
