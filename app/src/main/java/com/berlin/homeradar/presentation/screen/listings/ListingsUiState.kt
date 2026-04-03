package com.berlin.homeradar.presentation.screen.listings

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SyncInfo

data class ListingsUiState(
    val listings: List<HousingListing> = emptyList(),
    val syncInfo: SyncInfo = SyncInfo(),
    val showFavoritesOnly: Boolean = false,
    val minRooms: Double? = null,
    val district: String? = null,
    val isRefreshing: Boolean = false,
    val message: String? = null,
)
