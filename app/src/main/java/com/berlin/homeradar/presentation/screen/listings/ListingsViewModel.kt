package com.berlin.homeradar.presentation.screen.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.usecase.GetListingsUseCase
import com.berlin.homeradar.domain.usecase.ObserveSavedSearchesUseCase
import com.berlin.homeradar.domain.usecase.ObserveSyncInfoUseCase
import com.berlin.homeradar.domain.usecase.RefreshListingsUseCase
import com.berlin.homeradar.domain.usecase.SaveSearchUseCase
import com.berlin.homeradar.domain.usecase.ToggleFavoriteUseCase
import com.berlin.homeradar.presentation.screen.savedsearches.SavedSearchesViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    observeSyncInfoUseCase: ObserveSyncInfoUseCase,
    observeSavedSearchesUseCase: ObserveSavedSearchesUseCase,
    private val refreshListingsUseCase: RefreshListingsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveSearchUseCase: SaveSearchUseCase,
) : ViewModel() {

    private val filters = MutableStateFlow(ListingsFilters())

    val uiState = combine(
        filters,
        observeSyncInfoUseCase(),
        observeSavedSearchesUseCase(),
        filters.flatMapLatest { filter ->
            getListingsUseCase(
                onlyFavorites = filter.showFavoritesOnly,
                minRooms = null,
                district = null,
            )
        },
    ) { filter, syncInfo, savedSearches, listings ->
        val availableDistricts = listings.map { it.district }.distinct().sorted()
        val availableSources = listings.map { it.source }.distinct().sorted()
        val filteredListings = listings.filter { it.matches(filter) }
        val activeAlertsCount = savedSearches.count { saved ->
            saved.alertsEnabled && listings.any { it.matches(saved.filters) }
        }
        ListingsUiState(
            listings = filteredListings,
            syncInfo = syncInfo,
            showFavoritesOnly = filter.showFavoritesOnly,
            query = filter.query,
            minRooms = filter.minRooms,
            minArea = filter.minArea,
            maxPrice = filter.maxPrice,
            district = filter.district,
            selectedSourceIds = filter.selectedSourceIds,
            onlyJobcenter = filter.onlyJobcenter,
            onlyWohngeld = filter.onlyWohngeld,
            onlyWbs = filter.onlyWbs,
            availableDistricts = availableDistricts,
            availableSources = availableSources,
            savedSearches = savedSearches,
            activeAlertsCount = activeAlertsCount,
            isRefreshing = syncInfo.isSyncing,
            message = syncInfo.lastErrorMessage,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ListingsUiState(),
    )

    init {
        manualRefresh()
    }

    fun manualRefresh() {
        viewModelScope.launch {
            refreshListingsUseCase("manual")
        }
    }

    fun toggleFavorite(listingId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(listingId)
        }
    }

    fun saveCurrentSearch(name: String) {
        viewModelScope.launch {
            saveSearchUseCase(
                SavedSearchesViewModel.newSearch(name, filters.value.toPreset())
            )
        }
    }

    fun applySavedSearch(search: SavedSearch) {
        filters.value = ListingsFilters.fromPreset(search.filters)
    }

    fun toggleFavoritesOnly() { filters.value = filters.value.copy(showFavoritesOnly = !filters.value.showFavoritesOnly) }
    fun setQuery(value: String) { filters.value = filters.value.copy(query = value) }
    fun setMinRooms(value: Double?) { filters.value = filters.value.copy(minRooms = value) }
    fun setMinArea(value: Double?) { filters.value = filters.value.copy(minArea = value) }
    fun setMaxPrice(value: Int?) { filters.value = filters.value.copy(maxPrice = value) }
    fun setDistrict(value: String?) { filters.value = filters.value.copy(district = value) }
    fun toggleJobcenterFilter() { filters.value = filters.value.copy(onlyJobcenter = !filters.value.onlyJobcenter) }
    fun toggleWohngeldFilter() { filters.value = filters.value.copy(onlyWohngeld = !filters.value.onlyWohngeld) }
    fun toggleWbsFilter() { filters.value = filters.value.copy(onlyWbs = !filters.value.onlyWbs) }
    fun toggleSource(sourceId: String) {
        filters.value = filters.value.copy(
            selectedSourceIds = filters.value.selectedSourceIds.toMutableSet().apply { if (!add(sourceId)) remove(sourceId) }
        )
    }
    fun clearFilters() { filters.value = ListingsFilters(showFavoritesOnly = filters.value.showFavoritesOnly) }
}

data class ListingsFilters(
    val showFavoritesOnly: Boolean = false,
    val query: String = "",
    val minRooms: Double? = null,
    val minArea: Double? = null,
    val maxPrice: Int? = null,
    val district: String? = null,
    val selectedSourceIds: Set<String> = emptySet(),
    val onlyJobcenter: Boolean = false,
    val onlyWohngeld: Boolean = false,
    val onlyWbs: Boolean = false,
) {
    fun toPreset(): ListingFilterPreset = ListingFilterPreset(
        query = query,
        minRooms = minRooms,
        minArea = minArea,
        maxPrice = maxPrice,
        district = district,
        selectedSourceIds = selectedSourceIds,
        onlyJobcenter = onlyJobcenter,
        onlyWohngeld = onlyWohngeld,
        onlyWbs = onlyWbs,
        showFavoritesOnly = showFavoritesOnly,
    )

    companion object {
        fun fromPreset(preset: ListingFilterPreset): ListingsFilters = ListingsFilters(
            showFavoritesOnly = preset.showFavoritesOnly,
            query = preset.query,
            minRooms = preset.minRooms,
            minArea = preset.minArea,
            maxPrice = preset.maxPrice,
            district = preset.district,
            selectedSourceIds = preset.selectedSourceIds,
            onlyJobcenter = preset.onlyJobcenter,
            onlyWohngeld = preset.onlyWohngeld,
            onlyWbs = preset.onlyWbs,
        )
    }
}

fun HousingListing.matches(filter: ListingsFilters): Boolean = matches(filter.toPreset())

fun HousingListing.matches(filter: ListingFilterPreset): Boolean {
    val matchesQuery = filter.query.isBlank() || buildString {
        append(title); append(' '); append(location); append(' '); append(district); append(' '); append(source)
    }.contains(filter.query, ignoreCase = true)
    val matchesRooms = filter.minRooms == null || rooms >= filter.minRooms
    val matchesArea = filter.minArea == null || areaSqm >= filter.minArea
    val matchesPrice = filter.maxPrice == null || priceEuro <= filter.maxPrice
    val matchesDistrict = filter.district.isNullOrBlank() || district.equals(filter.district, ignoreCase = true)
    val matchesSource = filter.selectedSourceIds.isEmpty() || source in filter.selectedSourceIds
    val matchesJobcenter = !filter.onlyJobcenter || isJobcenterSuitable
    val matchesWohngeld = !filter.onlyWohngeld || isWohngeldEligible
    val matchesWbs = !filter.onlyWbs || isWbsRequired
    val matchesFavorites = !filter.showFavoritesOnly || isFavorite
    return matchesQuery && matchesRooms && matchesArea && matchesPrice &&
        matchesDistrict && matchesSource && matchesJobcenter &&
        matchesWohngeld && matchesWbs && matchesFavorites
}
