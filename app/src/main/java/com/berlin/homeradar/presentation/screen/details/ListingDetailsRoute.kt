package com.berlin.homeradar.presentation.screen.details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ListingDetailsRoute(
    onBack: () -> Unit,
    onOpenBrowser: (String) -> Unit,
    onOpenInApp: (String, String) -> Unit,
    onShare: (String) -> Unit,
    viewModel: ListingDetailsViewModel = hiltViewModel(),
) {
    ListingDetailsScreen(
        uiState = viewModel.uiState,
        onBack = onBack,
        onOpenBrowser = onOpenBrowser,
        onOpenInApp = onOpenInApp,
        onShare = onShare,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}
