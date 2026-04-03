package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ListingsRoute(
    onListingClick: (Long) -> Unit,
    onSavedSearchesClick: () -> Unit,
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    ListingsScreen(
        uiState = viewModel.uiState,
        onRefresh = viewModel::manualRefresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
        onQueryChanged = viewModel::setQuery,
        onMinRoomsSelected = viewModel::setMinRooms,
        onMinAreaSelected = viewModel::setMinArea,
        onMaxPriceSelected = viewModel::setMaxPrice,
        onDistrictSelected = viewModel::setDistrict,
        onToggleJobcenter = viewModel::toggleJobcenterFilter,
        onToggleWohngeld = viewModel::toggleWohngeldFilter,
        onToggleWbs = viewModel::toggleWbsFilter,
        onToggleSource = viewModel::toggleSource,
        onClearFilters = viewModel::clearFilters,
        onSaveSearch = viewModel::saveCurrentSearch,
        onApplySavedSearch = viewModel::applySavedSearch,
        onListingClick = onListingClick,
        onSavedSearchesClick = onSavedSearchesClick,
    )
}
