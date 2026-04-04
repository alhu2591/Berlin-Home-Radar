package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.berlin.homeradar.R
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.presentation.util.formatArea
import com.berlin.homeradar.presentation.util.formatPrice
import com.berlin.homeradar.presentation.util.formatRooms
import com.berlin.homeradar.presentation.util.formatTimestamp

@Composable
internal fun ListingsTopBar(
    resultCount: Int,
    activeAlertsCount: Int,
    onSavedSearchesClick: () -> Unit,
    onSaveSearchClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.app_name))
                Text(
                    text = stringResource(R.string.listings_results_count, resultCount),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        actions = {
            if (activeAlertsCount > 0) {
                AssistChip(
                    onClick = onSavedSearchesClick,
                    label = { Text(activeAlertsCount.toString()) },
                )
            }
            IconButton(onClick = onSavedSearchesClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = stringResource(R.string.cd_open_saved_searches))
            }
            IconButton(onClick = onSaveSearchClick) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = stringResource(R.string.cd_save_search))
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.cd_refresh_listings))
            }
        },
    )
}

@Composable
internal fun ListingsContent(
    state: ListingsUiState,
    padding: PaddingValues,
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
    onApplySavedSearch: (SavedSearch) -> Unit,
    onListingClick: (Long) -> Unit,
    onSavedSearchesClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        val wideLayout = maxWidth >= 960.dp
        if (state.isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (wideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SyncBanner(
                        lastSyncText = formatTimestamp(state.syncInfo.lastSuccessfulSyncMillis),
                        interval = state.syncInfo.syncInterval,
                        enabledSourcesCount = state.availableSources.size,
                        issueMessage = state.syncIssueMessage,
                    )
                    if (state.savedSearches.isNotEmpty()) {
                        SavedSearchRow(
                            searches = state.savedSearches,
                            onApplySavedSearch = onApplySavedSearch,
                            onSavedSearchesClick = onSavedSearchesClick,
                        )
                    }
                    FiltersSection(
                        state = state,
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
                    )
                }

                ListingsResultsPane(
                    state = state,
                    onRefresh = onRefresh,
                    onClearFilters = onClearFilters,
                    onToggleFavorite = onToggleFavorite,
                    onListingClick = onListingClick,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        SyncBanner(
                            lastSyncText = formatTimestamp(state.syncInfo.lastSuccessfulSyncMillis),
                            interval = state.syncInfo.syncInterval,
                            enabledSourcesCount = state.availableSources.size,
                            issueMessage = state.syncIssueMessage,
                        )
                    }

                    if (state.savedSearches.isNotEmpty()) {
                        item {
                            SavedSearchRow(
                                searches = state.savedSearches,
                                onApplySavedSearch = onApplySavedSearch,
                                onSavedSearchesClick = onSavedSearchesClick,
                            )
                        }
                    }

                    item {
                        FiltersSection(
                            state = state,
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
                        )
                    }

                    ResultsListContent(
                        state = state,
                        onRefresh = onRefresh,
                        onClearFilters = onClearFilters,
                        onToggleFavorite = onToggleFavorite,
                        onListingClick = onListingClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListingsResultsPane(
    state: ListingsUiState,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onListingClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ResultsListContent(
            state = state,
            onRefresh = onRefresh,
            onClearFilters = onClearFilters,
            onToggleFavorite = onToggleFavorite,
            onListingClick = onListingClick,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.ResultsListContent(
    state: ListingsUiState,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onListingClick: (Long) -> Unit,
) {
    if (state.listings.isEmpty() && state.isRefreshing) {
        items(3) {
            LoadingListingsCard()
        }
    } else if (state.listings.isEmpty()) {
        item {
            EmptyStateCard(
                hasActiveFilters = state.hasActiveFilters,
                issueMessage = state.syncIssueMessage,
                onClearFilters = onClearFilters,
                onRefresh = onRefresh,
            )
        }
    } else {
        items(state.listings, key = { it.id }) { listing ->
            ListingCard(
                listing = listing,
                onToggleFavorite = onToggleFavorite,
                onClick = { onListingClick(listing.id) },
            )
        }
    }
}

