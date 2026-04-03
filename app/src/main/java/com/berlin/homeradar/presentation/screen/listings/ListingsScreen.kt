@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
                title = {
                    Column {
                        Text("Berlin Home Radar")
                        Text(
                            text = "${state.listings.size} results",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                actions = {
                    if (state.activeAlertsCount > 0) {
                        AssistChip(
                            onClick = onSavedSearchesClick,
                            label = { Text("${state.activeAlertsCount}") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

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

                if (state.listings.isEmpty()) {
                    item {
                        EmptyStateCard(
                            loading = state.isRefreshing,
                            hasMessage = !state.message.isNullOrBlank(),
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
        }
    }
}

@Composable
private fun SyncBanner(
    lastSyncText: String,
    interval: SyncIntervalOption,
    enabledSourcesCount: Int,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Smart sync",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "WorkManager runs on ${intervalLabel(interval)} or manual mode. Android does not guarantee 5-minute execution.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Last sync: $lastSyncText • Enabled sources: $enabledSourcesCount",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SavedSearchRow(
    searches: List<SavedSearch>,
    onApplySavedSearch: (SavedSearch) -> Unit,
    onSavedSearchesClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Saved searches", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap a preset to reapply your filters instantly.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onSavedSearchesClick) {
                    Text("Manage")
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                searches.take(8).forEach { search ->
                    ElevatedAssistChip(
                        onClick = { onApplySavedSearch(search) },
                        label = { Text(search.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    loading: Boolean,
    hasMessage: Boolean,
) {
    Card {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("No listings match the current filters.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (hasMessage) {
                            "Some sources failed, but the app kept processing the others. Try broadening filters or refresh again later."
                        } else {
                            "Try a broader search, enable more sources, or refresh manually."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

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
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(
                title = "Filters",
                subtitle = "Refine by price, size, district, badges, favorites, and source.",
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                label = { Text("Search listings") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = state.showFavoritesOnly, onClick = onToggleFavoritesOnly, label = { Text("Favorites") })
                FilterChip(selected = state.onlyJobcenter, onClick = onToggleJobcenter, label = { Text("Jobcenter") })
                FilterChip(selected = state.onlyWohngeld, onClick = onToggleWohngeld, label = { Text("Wohngeld") })
                FilterChip(selected = state.onlyWbs, onClick = onToggleWbs, label = { Text("WBS") })
                TextButton(onClick = onClearFilters) { Text("Reset all") }
            }

            SectionTitle("Rooms")
            ChipGroup(
                labels = listOf("Any" to null, "1+" to 1.0, "2+" to 2.0, "3+" to 3.0, "4+" to 4.0),
                selected = state.minRooms,
                onSelect = onMinRoomsSelected,
            )

            SectionTitle("Area")
            ChipGroup(
                labels = listOf("Any" to null, "30+ m²" to 30.0, "50+ m²" to 50.0, "70+ m²" to 70.0, "90+ m²" to 90.0),
                selected = state.minArea,
                onSelect = onMinAreaSelected,
            )

            SectionTitle("Max rent")
            ChipGroup(
                labels = listOf("Any" to null, "€700" to 700, "€1000" to 1000, "€1300" to 1300, "€1600" to 1600),
                selected = state.maxPrice,
                onSelect = onMaxPriceSelected,
            )

            if (state.availableDistricts.isNotEmpty()) {
                SectionTitle("District")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(selected = state.district == null, onClick = { onDistrictSelected(null) }, label = { Text("All") })
                    state.availableDistricts.forEach { district ->
                        FilterChip(
                            selected = state.district == district,
                            onClick = { onDistrictSelected(district) },
                            label = { Text(district) },
                        )
                    }
                }
            }

            if (state.availableSources.isNotEmpty()) {
                SectionTitle("Sources", "The app keeps fetching from healthy sources even if one fails.")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
private fun <T> ChipGroup(
    labels: List<Pair<String, T?>>,
    selected: T?,
    onSelect: (T?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { (label, value) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun ListingCard(
    listing: HousingListing,
    onToggleFavorite: (Long) -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column {
            AsyncImage(
                model = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(listing.title, style = MaterialTheme.typography.titleMedium)
                        Text(listing.location, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${formatRooms(listing.rooms)} • ${formatArea(listing.areaSqm)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(listing.id) }) {
                        Icon(
                            imageVector = if (listing.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                        )
                    }
                }

                Text(
                    text = formatPrice(listing.priceEuro),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                AssistRow(listing)
            }
        }
    }
}

@Composable
private fun AssistRow(listing: HousingListing) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SourceBadge(SourceCatalog.nameFor(listing.source))
        if (listing.isJobcenterSuitable) AssistChip(onClick = {}, label = { Text("Jobcenter ✅") })
        if (listing.isWohngeldEligible) AssistChip(onClick = {}, label = { Text("Wohngeld ✅") })
        if (listing.isWbsRequired) AssistChip(onClick = {}, label = { Text("WBS") })
    }
}

@Composable
private fun SourceBadge(label: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
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
    SyncIntervalOption.MANUAL -> "manual mode"
    SyncIntervalOption.MINUTES_15 -> "15 minutes"
    SyncIntervalOption.MINUTES_30 -> "30 minutes"
    SyncIntervalOption.HOUR_1 -> "1 hour"
    SyncIntervalOption.HOURS_3 -> "3 hours"
}
