package com.berlin.homeradar.presentation.screen.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.berlin.homeradar.R
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import com.berlin.homeradar.presentation.util.formatArea
import com.berlin.homeradar.presentation.util.formatPrice
import com.berlin.homeradar.presentation.util.formatRooms
import com.berlin.homeradar.presentation.util.formatTimestamp
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
    val state by uiState.collectAsStateWithLifecycle()
    val listing = state.listing
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        listing?.title ?: stringResource(R.string.details_fallback_title),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    if (listing != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (listing.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(if (listing.isFavorite) R.string.remove_favorite else R.string.save_favorite),
                            )
                        }
                    }
                },
            )
        }
    ) { padding ->
        if (listing == null) return@Scaffold
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            val wideLayout = maxWidth >= 840.dp
            if (wideLayout) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ListingHeroCard(listing = listing)
                    }
                    Column(
                        modifier = Modifier.width(320.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ListingInfoCard(listing = listing)
                        ListingActionColumn(
                            listingUrl = listing.listingUrl,
                            title = listing.title,
                            isFavorite = listing.isFavorite,
                            onOpenInApp = onOpenInApp,
                            onOpenBrowser = onOpenBrowser,
                            onShare = onShare,
                            onToggleFavorite = onToggleFavorite,
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ListingHeroCard(listing = listing)
                    ListingInfoCard(listing = listing)
                    ListingActionColumn(
                        listingUrl = listing.listingUrl,
                        title = listing.title,
                        isFavorite = listing.isFavorite,
                        onOpenInApp = onOpenInApp,
                        onOpenBrowser = onOpenBrowser,
                        onShare = onShare,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListingHeroCard(listing: com.berlin.homeradar.domain.model.HousingListing) {
    ElevatedCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = listing.imageUrl,
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    listing.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                LifecycleBadge(listing.lifecycleStatus)
                Text(
                    formatPrice(listing.priceEuro),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(listing.location, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        stringResource(R.string.details_stat_rooms),
                        formatRooms(listing.rooms),
                        Modifier.weight(1f),
                    )
                    StatCard(
                        stringResource(R.string.details_stat_area),
                        formatArea(listing.areaSqm),
                        Modifier.weight(1f),
                    )
                }
                Text(
                    stringResource(R.string.listing_status_last_seen, formatTimestamp(listing.lastSeenAtEpochMillis)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ListingInfoCard(listing: com.berlin.homeradar.domain.model.HousingListing) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.details_section_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(
                    R.string.details_source_format,
                    SourceCatalog.nameFor(listing.source),
                ),
            )
            Text(stringResource(R.string.details_district_format, listing.district))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (listing.isJobcenterSuitable) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.badge_jobcenter)) })
                }
                if (listing.isWohngeldEligible) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.badge_wohngeld)) })
                }
                if (listing.isWbsRequired) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.badge_wbs)) })
                }
            }
        }
    }
}

@Composable
private fun ListingActionColumn(
    listingUrl: String,
    title: String,
    isFavorite: Boolean,
    onOpenBrowser: (String) -> Unit,
    onOpenInApp: (String, String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = { onOpenInApp(title, listingUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Language, contentDescription = stringResource(R.string.open_inside_app))
            Text(
                stringResource(R.string.open_inside_app),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OutlinedButton(
            onClick = { onOpenBrowser(listingUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.OpenInBrowser, contentDescription = stringResource(R.string.open_in_browser))
            Text(
                stringResource(R.string.open_in_browser),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OutlinedButton(
            onClick = { onShare(listingUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share_label))
            Text(
                stringResource(R.string.share_label),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(
                    if (isFavorite) R.string.remove_favorite else R.string.save_favorite,
                ),
            )
        }
    }
}

@Composable
private fun LifecycleBadge(status: ListingLifecycleStatus) {
    val label = when (status) {
        ListingLifecycleStatus.ACTIVE -> stringResource(R.string.listing_status_active)
        ListingLifecycleStatus.STALE -> stringResource(R.string.listing_status_stale)
        ListingLifecycleStatus.ARCHIVED -> stringResource(R.string.listing_status_archived)
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}
