package com.berlin.homeradar.presentation.screen.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        filters.flatMapLatest { filter -> getListingsUseCase(filter.toPreset()) },
        getListingsUseCase.allActive(),
    ) { filter, syncInfo, savedSearches, filteredListings, allActiveListings ->
        val availableDistricts = allActiveListings.map { it.district }.distinct().sorted()
        val availableSources = allActiveListings.map { it.source }.distinct().sorted()
        val activeAlertsCount = savedSearches.count { saved ->
            saved.alertsEnabled && allActiveListings.any { listing -> listing.matchesFilter(saved.filters) }
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
            hasActiveFilters = filter.hasActiveCriteria(),
            syncIssueMessage = syncInfo.lastErrorMessage?.takeIf { it.isNotBlank() },
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

    fun toggleFavoritesOnly() {
        filters.value = filters.value.copy(showFavoritesOnly = !filters.value.showFavoritesOnly)
    }

    fun setQuery(value: String) {
        filters.value = filters.value.copy(query = value)
    }

    fun setMinRooms(value: Double?) {
        filters.value = filters.value.copy(minRooms = value)
    }

    fun setMinArea(value: Double?) {
        filters.value = filters.value.copy(minArea = value)
    }

    fun setMaxPrice(value: Int?) {
        filters.value = filters.value.copy(maxPrice = value)
    }

    fun setDistrict(value: String?) {
        filters.value = filters.value.copy(district = value)
    }

    fun toggleSource(sourceId: String) {
        val selected = filters.value.selectedSourceIds.toMutableSet()
        if (!selected.add(sourceId)) selected.remove(sourceId)
        filters.value = filters.value.copy(selectedSourceIds = selected)
    }

    fun toggleJobcenterFilter() {
        filters.value = filters.value.copy(onlyJobcenter = !filters.value.onlyJobcenter)
    }

    fun toggleWohngeldFilter() {
        filters.value = filters.value.copy(onlyWohngeld = !filters.value.onlyWohngeld)
    }

    fun toggleWbsFilter() {
        filters.value = filters.value.copy(onlyWbs = !filters.value.onlyWbs)
    }

    fun clearFilters() {
        filters.value = ListingsFilters()
    }
}
