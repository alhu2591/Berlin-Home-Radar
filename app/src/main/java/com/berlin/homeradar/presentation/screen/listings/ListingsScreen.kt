package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.presentation.util.formatArea
import com.berlin.homeradar.presentation.util.formatPrice
import com.berlin.homeradar.presentation.util.formatRooms
import com.berlin.homeradar.presentation.util.formatTimestamp
import kotlinx.coroutines.flow.StateFlow

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
    val state by uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbarHostState.showSnackbar(it) }
    }

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
            TopAppBar(
                title = { Text("Berlin Home Radar") },
                actions = {
                    if (state.activeAlertsCount > 0) {
                        AssistChip(
                            onClick = onSavedSearchesClick,
                            label = { Text("${state.activeAlertsCount}") }
                        )
                    }
                    IconButton(onClick = onSavedSearchesClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null)
                    }
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Outlined.BookmarkAdd, contentDescription = null)
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            SyncBanner(lastSyncText = formatTimestamp(state.syncInfo.lastSuccessfulSyncMillis), interval = state.syncInfo.syncInterval)
            SavedSearchRow(
                searches = state.savedSearches,
                onApplySavedSearch = onApplySavedSearch,
            )
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
            if (state.listings.isEmpty() && state.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.listings, key = { it.id }) { listing ->
                        ListingCard(
                            listing = listing,
                            onToggleFavorite = onToggleFavorite,
                            onClick = { onListingClick(listing.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedSearchRow(
    searches: List<SavedSearch>,
    onApplySavedSearch: (SavedSearch) -> Unit,
) {
    if (searches.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text("Saved searches")
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            searches.take(8).forEach { search ->
                ElevatedAssistChip(
                    onClick = { onApplySavedSearch(search) },
                    label = { Text(search.name) },
                )
            }
        }
    }
}

@Composable
private fun SyncBanner(lastSyncText: String, interval: SyncIntervalOption) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
            text = "WorkManager sync: ${intervalLabel(interval)}. Android does not guarantee 5-minute execution. Last sync: $lastSyncText",
            modifier = Modifier.padding(12.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersSection(
    state: ListingsUiState,
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
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.showFavoritesOnly, onClick = onToggleFavoritesOnly, label = { Text("Favorites") })
                FilterChip(selected = state.onlyJobcenter, onClick = onToggleJobcenter, label = { Text("Jobcenter ✅") })
                FilterChip(selected = state.onlyWohngeld, onClick = onToggleWohngeld, label = { Text("Wohngeld ✅") })
                FilterChip(selected = state.onlyWbs, onClick = onToggleWbs, label = { Text("WBS") })
                FilterChip(selected = false, onClick = onClearFilters, label = { Text("Clear") })
            }
            Text("Rooms")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf<Double?>(null, 1.0, 2.0, 3.0, 4.0).forEach { value ->
                    FilterChip(
                        selected = state.minRooms == value,
                        onClick = { onMinRoomsSelected(value) },
                        label = { Text(value?.let { "${it.toInt()}+" } ?: "Any") },
                    )
                }
            }
            Text("Area")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf<Int?>(null, 30, 50, 70, 90).forEach { value ->
                    FilterChip(
                        selected = state.minArea == value?.toDouble(),
                        onClick = { onMinAreaSelected(value?.toDouble()) },
                        label = { Text(value?.let { "${it}+ m²" } ?: "Any") },
                    )
                }
            }
            Text("Max price")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf<Int?>(null, 700, 1000, 1300, 1600).forEach { value ->
                    FilterChip(
                        selected = state.maxPrice == value,
                        onClick = { onMaxPriceSelected(value) },
                        label = { Text(value?.let { "≤ €$it" } ?: "Any") },
                    )
                }
            }
            if (state.availableDistricts.isNotEmpty()) {
                Text("District")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.district == null, onClick = { onDistrictSelected(null) }, label = { Text("All") })
                    state.availableDistricts.forEach { district ->
                        FilterChip(selected = state.district == district, onClick = { onDistrictSelected(district) }, label = { Text(district) })
                    }
                }
            }
            if (state.availableSources.isNotEmpty()) {
                Text("Sources")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.availableSources.forEach { sourceId ->
                        FilterChip(
                            selected = sourceId in state.selectedSourceIds,
                            onClick = { onToggleSource(sourceId) },
                            label = { Text(SourceCatalog.nameFor(sourceId)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: HousingListing,
    onToggleFavorite: (Long) -> Unit,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        ListItem(
            headlineContent = { Text(listing.title) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(model = listing.imageUrl, contentDescription = listing.title, modifier = Modifier.fillMaxWidth())
                    Text(formatPrice(listing.priceEuro))
                    Text(listing.location)
                    Text("${formatRooms(listing.rooms)} • ${formatArea(listing.areaSqm)}")
                    AssistRow(listing)
                }
            },
            trailingContent = {
                IconButton(onClick = { onToggleFavorite(listing.id) }) {
                    Icon(
                        imageVector = if (listing.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssistRow(listing: HousingListing) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ElevatedAssistChip(onClick = {}, label = { Text(SourceCatalog.nameFor(listing.source)) })
        if (listing.isJobcenterSuitable) AssistChip(onClick = {}, label = { Text("Jobcenter ✅") })
        if (listing.isWohngeldEligible) AssistChip(onClick = {}, label = { Text("Wohngeld ✅") })
        if (listing.isWbsRequired) AssistChip(onClick = {}, label = { Text("WBS") })
    }
}

@Composable
private fun SaveSearchDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save current filters") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Saved search name") },
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name.ifBlank { "My search" }) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun intervalLabel(option: SyncIntervalOption): String = when (option) {
    SyncIntervalOption.MANUAL -> "Manual"
    SyncIntervalOption.MINUTES_15 -> "15 min"
    SyncIntervalOption.MINUTES_30 -> "30 min"
    SyncIntervalOption.HOURLY -> "1 hour"
    SyncIntervalOption.THREE_HOURLY -> "3 hours"
}
