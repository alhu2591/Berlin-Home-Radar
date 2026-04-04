@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.presentation.theme.BerlinHomeRadarTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.Scaffold

@Composable
fun ListingsScreen(
    uiState: StateFlow<ListingsUiState>,
    onRefresh: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onMinRoomsSelected: (Double?) -> Unit,
    onMinAreaSelected: (Double?) -> Unit,
    onMaxPriceSelected: (Int?) -> Unit,
    onDistrictSelected: (String?) -> Unit,
    onToggleJobcenter: () -> Unit,
    onToggleWohngeld: () -> Unit,
    onToggleWbs: () -> Unit,
    onToggleSource: (String) -> Unit,
    onClearFilters: () -> Unit,
    onSaveSearch: (String) -> Unit,
    onApplySavedSearch: (SavedSearch) -> Unit,
    onListingClick: (Long) -> Unit,
    onSavedSearchesClick: () -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }

    if (showSaveDialog) {
        SaveSearchDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                onSaveSearch(name)
                showSaveDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            ListingsTopBar(
                resultCount = state.listings.size,
                activeAlertsCount = state.activeAlertsCount,
                onSavedSearchesClick = onSavedSearchesClick,
                onSaveSearchClick = { showSaveDialog = true },
                onRefresh = onRefresh,
            )
        },
    ) { padding ->
        ListingsContent(
            state = state,
            padding = padding,
            onRefresh = onRefresh,
            onToggleFavorite = onToggleFavorite,
            onToggleFavoritesOnly = onToggleFavoritesOnly,
            onQueryChanged = onQueryChanged,
            onMinRoomsSelected = onMinRoomsSelected,
            onMinAreaSelected = onMinAreaSelected,
            onMaxPriceSelected = onMaxPriceSelected,
            onDistrictSelected = onDistrictSelected,
            onToggleJobcenter = onToggleJobcenter,
            onToggleWohngeld = onToggleWohngeld,
            onToggleWbs = onToggleWbs,
            onToggleSource = onToggleSource,
            onClearFilters = onClearFilters,
            onApplySavedSearch = onApplySavedSearch,
            onListingClick = onListingClick,
            onSavedSearchesClick = onSavedSearchesClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListingCardPreview() {
    BerlinHomeRadarTheme {
        ListingCard(
            listing = HousingListing(
                id = 1L,
                source = "bundled-json",
                externalId = "sample-1",
                title = "Bright 2-room flat",
                priceEuro = 1250,
                district = "Mitte",
                location = "Berlin",
                rooms = 2.0,
                areaSqm = 58.0,
                imageUrl = null,
                listingUrl = "https://example.com/listing/1",
                isJobcenterSuitable = true,
                isWohngeldEligible = true,
                isWbsRequired = false,
                isFavorite = true,
                updatedAtEpochMillis = 0L,
            ),
            onToggleFavorite = {},
            onClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 860)
@Composable
private fun ListingsScreenPreview() {
    BerlinHomeRadarTheme {
        ListingsScreen(
            uiState = MutableStateFlow(
                ListingsUiState(
                    listings = listOf(
                        HousingListing(
                            id = 1L,
                            source = "bundled-json",
                            externalId = "sample-1",
                            title = "Quiet apartment near U-Bahn",
                            priceEuro = 1180,
                            district = "Neukölln",
                            location = "Berlin",
                            rooms = 2.5,
                            areaSqm = 63.0,
                            imageUrl = null,
                            listingUrl = "https://example.com/listing/1",
                            isJobcenterSuitable = false,
                            isWohngeldEligible = true,
                            isWbsRequired = false,
                            isFavorite = false,
                            updatedAtEpochMillis = 0L,
                        )
                    ),
                    availableDistricts = listOf("Mitte", "Neukölln"),
                    availableSources = listOf("bundled-json", "remote-json"),
                    selectedSourceIds = setOf("bundled-json"),
                    savedSearches = listOf(
                        SavedSearch(
                            id = "search-1",
                            name = "2 rooms under €1300",
                            filters = ListingFilterPreset(
                                minRooms = 2.0,
                                maxPrice = 1300,
                            ),
                        )
                    ),
                    activeAlertsCount = 2,
                    hasActiveFilters = true,
                )
            ),
            onRefresh = {},
            onToggleFavorite = {},
            onToggleFavoritesOnly = {},
            onQueryChanged = {},
            onMinRoomsSelected = {},
            onMinAreaSelected = {},
            onMaxPriceSelected = {},
            onDistrictSelected = {},
            onToggleJobcenter = {},
            onToggleWohngeld = {},
            onToggleWbs = {},
            onToggleSource = {},
            onClearFilters = {},
            onSaveSearch = {},
            onApplySavedSearch = {},
            onListingClick = {},
            onSavedSearchesClick = {},
        )
    }
}
