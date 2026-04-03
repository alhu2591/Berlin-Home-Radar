package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ListingFilterPreset(
    val query: String = "",
    val minRooms: Double? = null,
    val minArea: Double? = null,
    val maxPrice: Int? = null,
    val district: String? = null,
    val selectedSourceIds: Set<String> = emptySet(),
    val onlyJobcenter: Boolean = false,
    val onlyWohngeld: Boolean = false,
    val onlyWbs: Boolean = false,
    val showFavoritesOnly: Boolean = false,
)
