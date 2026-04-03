package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    SettingsScreen(
        uiState = viewModel.uiState,
        onBackgroundSyncChanged = viewModel::onBackgroundSyncChanged,
        onRemoteSourceChanged = viewModel::onRemoteSourceChanged,
        onManualRefresh = viewModel::manualRefresh,
    )
}
