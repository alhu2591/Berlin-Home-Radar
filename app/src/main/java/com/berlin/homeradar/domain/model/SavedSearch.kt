package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedSearch(
    val id: String,
    val name: String,
    val filters: ListingFilterPreset,
    val alertsEnabled: Boolean = true,
)
