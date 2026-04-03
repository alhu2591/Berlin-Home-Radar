package com.berlin.homeradar.presentation.screen.savedsearches

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SavedSearchesRoute(
    onBack: () -> Unit,
    viewModel: SavedSearchesViewModel = hiltViewModel(),
) {
    SavedSearchesScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onDeleteSearch = viewModel::delete,
        onAlertEnabledChanged = viewModel::setAlertEnabled,
    )
}
