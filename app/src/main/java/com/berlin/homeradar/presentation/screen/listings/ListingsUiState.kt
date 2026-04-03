package com.berlin.homeradar.presentation.screen.listings

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SyncInfo

data class ListingsUiState(
    val listings: List<HousingListing> = emptyList(),
    val syncInfo: SyncInfo = SyncInfo(),
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
    val availableDistricts: List<String> = emptyList(),
    val availableSources: List<String> = emptyList(),
    val savedSearches: List<SavedSearch> = emptyList(),
    val activeAlertsCount: Int = 0,
    val isRefreshing: Boolean = false,
    val message: String? = null,
)
