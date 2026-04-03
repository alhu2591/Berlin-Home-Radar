package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    onManageSources: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    SettingsScreen(
        uiState = viewModel.uiState,
        onBackgroundSyncChanged = viewModel::onBackgroundSyncChanged,
        onRemoteSourceChanged = viewModel::onRemoteSourceChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
        onThemeSelected = viewModel::onThemeSelected,
        onSyncIntervalSelected = viewModel::onSyncIntervalSelected,
        onManageSources = onManageSources,
        onManualRefresh = viewModel::manualRefresh,
    )
}
