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
internal fun SyncBanner(
    lastSyncText: String,
    interval: SyncIntervalOption,
    enabledSourcesCount: Int,
    issueMessage: String?,
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
                text = stringResource(R.string.listings_sync_banner_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.listings_sync_banner_body, intervalLabel(interval)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.listings_sync_banner_meta, lastSyncText, enabledSourcesCount),
                style = MaterialTheme.typography.labelLarge,
            )
            if (!issueMessage.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.listings_sync_issue_message, issueMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
internal fun SavedSearchRow(
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
                    Text(
                        stringResource(R.string.listings_saved_searches_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(R.string.listings_saved_searches_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onSavedSearchesClick) {
                    Text(stringResource(R.string.manage_label))
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
internal fun LoadingListingsCard() {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.listings_loading_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.listings_loading_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
internal fun EmptyStateCard(
    hasActiveFilters: Boolean,
    issueMessage: String?,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (hasActiveFilters) {
                    stringResource(R.string.listings_empty_filtered_title)
                } else {
                    stringResource(R.string.listings_empty_unfiltered_title)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (hasActiveFilters) {
                    stringResource(R.string.listings_empty_filtered_body)
                } else {
                    stringResource(R.string.listings_empty_unfiltered_body)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!issueMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            stringResource(R.string.listings_sync_issue_title),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            stringResource(R.string.listings_sync_issue_body),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            stringResource(R.string.listings_sync_issue_message, issueMessage),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefresh) {
                    Text(stringResource(R.string.listings_refresh_again))
                }
                if (hasActiveFilters) {
                    TextButton(onClick = onClearFilters) {
                        Text(stringResource(R.string.clear_filters))
                    }
                }
            }
        }
    }
}
