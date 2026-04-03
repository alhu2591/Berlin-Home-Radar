package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ListingsRoute(
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    ListingsScreen(
        uiState = viewModel.uiState,
        onRefresh = viewModel::manualRefresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
        onMinRoomsSelected = viewModel::setMinRooms,
    )
}
