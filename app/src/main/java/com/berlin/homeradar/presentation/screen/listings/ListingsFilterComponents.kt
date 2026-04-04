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
internal fun FiltersSection(
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
            ListingsSectionTitle(
                title = stringResource(R.string.listings_filters_title),
                subtitle = stringResource(R.string.listings_filters_subtitle),
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                label = { Text(stringResource(R.string.search_listings_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            FilterToggleRow(
                showFavoritesOnly = state.showFavoritesOnly,
                onlyJobcenter = state.onlyJobcenter,
                onlyWohngeld = state.onlyWohngeld,
                onlyWbs = state.onlyWbs,
                onToggleFavoritesOnly = onToggleFavoritesOnly,
                onToggleJobcenter = onToggleJobcenter,
                onToggleWohngeld = onToggleWohngeld,
                onToggleWbs = onToggleWbs,
                onClearFilters = onClearFilters,
            )

            ListingsSectionTitle(stringResource(R.string.filters_rooms))
            ChipGroup(
                labels = listOf(
                    stringResource(R.string.any_label) to null,
                    stringResource(R.string.rooms_plus_label, 1) to 1.0,
                    stringResource(R.string.rooms_plus_label, 2) to 2.0,
                    stringResource(R.string.rooms_plus_label, 3) to 3.0,
                    stringResource(R.string.rooms_plus_label, 4) to 4.0,
                ),
                selected = state.minRooms,
                onSelect = onMinRoomsSelected,
            )

            ListingsSectionTitle(stringResource(R.string.filters_area))
            ChipGroup(
                labels = listOf(
                    stringResource(R.string.any_label) to null,
                    stringResource(R.string.area_plus_label, 30) to 30.0,
                    stringResource(R.string.area_plus_label, 50) to 50.0,
                    stringResource(R.string.area_plus_label, 70) to 70.0,
                    stringResource(R.string.area_plus_label, 90) to 90.0,
                ),
                selected = state.minArea,
                onSelect = onMinAreaSelected,
            )

            ListingsSectionTitle(stringResource(R.string.filters_price))
            ChipGroup(
                labels = listOf(
                    stringResource(R.string.any_label) to null,
                    stringResource(R.string.max_price_label, 700) to 700,
                    stringResource(R.string.max_price_label, 1000) to 1000,
                    stringResource(R.string.max_price_label, 1300) to 1300,
                    stringResource(R.string.max_price_label, 1600) to 1600,
                ),
                selected = state.maxPrice,
                onSelect = onMaxPriceSelected,
            )

            if (state.availableDistricts.isNotEmpty()) {
                DistrictFilterGroup(
                    availableDistricts = state.availableDistricts,
                    selectedDistrict = state.district,
                    onDistrictSelected = onDistrictSelected,
                )
            }

            if (state.availableSources.isNotEmpty()) {
                SourceFilterGroup(
                    availableSources = state.availableSources,
                    selectedSourceIds = state.selectedSourceIds,
                    onToggleSource = onToggleSource,
                )
            }
        }
    }
}

@Composable
internal fun FilterToggleRow(
    showFavoritesOnly: Boolean,
    onlyJobcenter: Boolean,
    onlyWohngeld: Boolean,
    onlyWbs: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    onToggleJobcenter: () -> Unit,
    onToggleWohngeld: () -> Unit,
    onToggleWbs: () -> Unit,
    onClearFilters: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = showFavoritesOnly,
            onClick = onToggleFavoritesOnly,
            label = { Text(stringResource(R.string.filter_favorites)) },
        )
        FilterChip(
            selected = onlyJobcenter,
            onClick = onToggleJobcenter,
            label = { Text(stringResource(R.string.filter_jobcenter)) },
        )
        FilterChip(
            selected = onlyWohngeld,
            onClick = onToggleWohngeld,
            label = { Text(stringResource(R.string.filter_wohngeld)) },
        )
        FilterChip(
            selected = onlyWbs,
            onClick = onToggleWbs,
            label = { Text(stringResource(R.string.badge_wbs)) },
        )
        TextButton(onClick = onClearFilters) {
            Text(stringResource(R.string.reset_all_label))
        }
    }
}

@Composable
internal fun DistrictFilterGroup(
    availableDistricts: List<String>,
    selectedDistrict: String?,
    onDistrictSelected: (String?) -> Unit,
) {
    ListingsSectionTitle(stringResource(R.string.filters_district))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedDistrict == null,
            onClick = { onDistrictSelected(null) },
            label = { Text(stringResource(R.string.district_all)) },
        )
        availableDistricts.forEach { district ->
            FilterChip(
                selected = selectedDistrict == district,
                onClick = { onDistrictSelected(district) },
                label = { Text(district) },
            )
        }
    }
}

@Composable
internal fun SourceFilterGroup(
    availableSources: List<String>,
    selectedSourceIds: Set<String>,
    onToggleSource: (String) -> Unit,
) {
    ListingsSectionTitle(
        title = stringResource(R.string.filters_sources),
        subtitle = stringResource(R.string.listings_sources_subtitle),
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableSources.forEach { sourceId ->
            FilterChip(
                selected = sourceId in selectedSourceIds,
                onClick = { onToggleSource(sourceId) },
                label = { Text(SourceCatalog.nameFor(sourceId)) },
            )
        }
    }
}

@Composable
internal fun ListingsSectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun <T> ChipGroup(
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
internal fun intervalLabel(option: SyncIntervalOption): String = when (option) {
    SyncIntervalOption.MANUAL -> stringResource(R.string.sync_manual)
    SyncIntervalOption.MINUTES_15 -> stringResource(R.string.sync_15m)
    SyncIntervalOption.MINUTES_30 -> stringResource(R.string.sync_30m)
    SyncIntervalOption.HOUR_1 -> stringResource(R.string.sync_1h)
    SyncIntervalOption.HOURS_3 -> stringResource(R.string.sync_3h)
}
