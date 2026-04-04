package com.berlin.homeradar.presentation.screen.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
    onManageSources: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message.resolve(context))
        }
    }

    SettingsScreen(
        uiState = viewModel.uiState,
        snackbarHostState = snackbarHostState,
        onBackgroundSyncChanged = viewModel::onBackgroundSyncChanged,
        onRemoteSourceChanged = viewModel::onRemoteSourceChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
        onThemeSelected = viewModel::onThemeSelected,
        onSyncIntervalSelected = viewModel::onSyncIntervalSelected,
        onManageSources = onManageSources,
        onManualRefresh = viewModel::manualRefresh,
        onRefreshRemoteConfig = viewModel::refreshRemoteConfig,
    )
}
