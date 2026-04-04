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
internal fun ListingCard(
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
                ListingCardHeader(listing = listing, onToggleFavorite = onToggleFavorite)
                Text(
                    text = formatPrice(listing.priceEuro),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                ListingBadgesRow(listing)
                Text(
                    text = stringResource(R.string.listing_status_last_seen, formatTimestamp(listing.lastSeenAtEpochMillis)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun ListingCardHeader(
    listing: HousingListing,
    onToggleFavorite: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(listing.title, style = MaterialTheme.typography.titleMedium)
            Text(listing.location, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(
                    R.string.listing_meta_summary,
                    formatRooms(listing.rooms),
                    formatArea(listing.areaSqm),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            LifecycleBadge(listing.lifecycleStatus)
        }
        IconButton(onClick = { onToggleFavorite(listing.id) }) {
            Icon(
                imageVector = if (listing.isFavorite) {
                    Icons.Outlined.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = if (listing.isFavorite) stringResource(R.string.cd_remove_favorite) else stringResource(R.string.cd_add_favorite),
            )
        }
    }
}

@Composable
internal fun ListingBadgesRow(listing: HousingListing) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SourceBadge(SourceCatalog.nameFor(listing.source))
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

@Composable
internal fun LifecycleBadge(status: ListingLifecycleStatus) {
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
internal fun SourceBadge(label: String) {
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
