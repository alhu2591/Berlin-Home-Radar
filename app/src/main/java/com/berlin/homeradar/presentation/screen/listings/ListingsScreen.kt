package com.berlin.homeradar.presentation.screen.listings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.berlin.homeradar.domain.model.HousingListing
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
    onMinRoomsSelected: (Double?) -> Unit,
) {
    val state by uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbarHostState.showSnackbar(it) }
    }

    ListingsScreenContent(
        state = state,
        onRefresh = onRefresh,
        onToggleFavorite = onToggleFavorite,
        onToggleFavoritesOnly = onToggleFavoritesOnly,
        onMinRoomsSelected = onMinRoomsSelected,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingsScreenContent(
    state: ListingsUiState,
    onRefresh: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onMinRoomsSelected: (Double?) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Berlin Home Radar") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(),
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

            SyncBanner(
                lastSyncText = formatTimestamp(state.syncInfo.lastSuccessfulSyncMillis)
            )

            FiltersRow(
                showFavoritesOnly = state.showFavoritesOnly,
                minRooms = state.minRooms,
                onToggleFavoritesOnly = onToggleFavoritesOnly,
                onMinRoomsSelected = onMinRoomsSelected,
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
                            onToggleFavorite = { onToggleFavorite(listing.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncBanner(lastSyncText: String) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Background sync uses WorkManager",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Android does not reliably run every 5 minutes. This app uses the platform-safe 15 minute minimum and always offers manual refresh. Last sync: $lastSyncText",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersRow(
    showFavoritesOnly: Boolean,
    minRooms: Double?,
    onToggleFavoritesOnly: () -> Unit,
    onMinRoomsSelected: (Double?) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = showFavoritesOnly,
            onClick = onToggleFavoritesOnly,
            label = { Text("Favorites") },
        )
        listOf<Double?>(null, 1.0, 2.0, 3.0).forEach { rooms ->
            FilterChip(
                selected = minRooms == rooms,
                onClick = { onMinRoomsSelected(rooms) },
                label = { Text(if (rooms == null) "Any rooms" else "${rooms.toInt()}+ rooms") },
            )
        }
    }
}

@Composable
private fun ListingCard(
    listing: HousingListing,
    onToggleFavorite: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!listing.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                )
            }

            ListItem(
                headlineContent = {
                    Text(
                        text = listing.title,
                        fontWeight = FontWeight.Bold,
                    )
                },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${formatPrice(listing.priceEuro)} • ${listing.location} • ${listing.district}")
                        Text("${formatRooms(listing.rooms)} • ${formatArea(listing.areaSqm)}")
                        BadgesRow(listing)
                    }
                },
                trailingContent = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (listing.isFavorite) {
                                Icons.Outlined.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = "Favorite",
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun BadgesRow(listing: HousingListing) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (listing.isJobcenterSuitable) {
            ElevatedAssistChip(onClick = {}, label = { Text("Jobcenter ✅") })
        }
        if (listing.isWohngeldEligible) {
            AssistChip(onClick = {}, label = { Text("Wohngeld ✅") })
        }
        if (listing.isWbsRequired) {
            AssistChip(onClick = {}, label = { Text("WBS") })
        }
    }
}
