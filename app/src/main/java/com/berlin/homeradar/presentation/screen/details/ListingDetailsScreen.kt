package com.berlin.homeradar.presentation.screen.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.berlin.homeradar.presentation.util.formatArea
import com.berlin.homeradar.presentation.util.formatPrice
import com.berlin.homeradar.presentation.util.formatRooms
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailsScreen(
    uiState: StateFlow<ListingDetailsUiState>,
    onBack: () -> Unit,
    onOpenBrowser: (String) -> Unit,
    onOpenInApp: (String, String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val state by uiState.collectAsState()
    val listing = state.listing
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listing?.title ?: "Listing details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = null) }
                },
                actions = {
                    if (listing != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (listing.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (listing == null) return@Scaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = listing.imageUrl,
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                    )
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(listing.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(formatPrice(listing.priceEuro), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        Text(listing.location, style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard("Rooms", formatRooms(listing.rooms), Modifier.weight(1f))
                            StatCard("Area", formatArea(listing.areaSqm), Modifier.weight(1f))
                        }
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Listing details", style = MaterialTheme.typography.titleMedium)
                    Text("Source: ${listing.source}")
                    Text("District: ${listing.district}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (listing.isJobcenterSuitable) AssistChip(onClick = {}, label = { Text("Jobcenter ✅") })
                        if (listing.isWohngeldEligible) AssistChip(onClick = {}, label = { Text("Wohngeld ✅") })
                        if (listing.isWbsRequired) AssistChip(onClick = {}, label = { Text("WBS") })
                    }
                }
            }

            FilledTonalButton(onClick = { onOpenInApp(listing.title, listing.listingUrl) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Language, contentDescription = null)
                Text("Open inside app", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = { onOpenBrowser(listing.listingUrl) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.OpenInBrowser, contentDescription = null)
                Text("Open in browser", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = { onShare(listing.listingUrl) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Text("Share", modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                Text(if (listing.isFavorite) "Remove favorite" else "Save favorite")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}
