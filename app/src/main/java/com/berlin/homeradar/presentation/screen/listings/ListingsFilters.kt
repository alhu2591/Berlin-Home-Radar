package com.berlin.homeradar.presentation.screen.listings

import com.berlin.homeradar.domain.filter.matchesFilter
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.ListingFilterPreset

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
    fun hasActiveCriteria(): Boolean = query.isNotBlank() ||
        minRooms != null ||
        minArea != null ||
        maxPrice != null ||
        !district.isNullOrBlank() ||
        selectedSourceIds.isNotEmpty() ||
        onlyJobcenter ||
        onlyWohngeld ||
        onlyWbs ||
        showFavoritesOnly

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


fun HousingListing.matchesFilter(filter: ListingsFilters): Boolean = matchesFilter(filter.toPreset())
