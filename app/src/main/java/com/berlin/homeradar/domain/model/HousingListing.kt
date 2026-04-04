package com.berlin.homeradar.domain.model

data class HousingListing(
    val id: Long = 0L,
    val source: String,
    val externalId: String,
    val title: String,
    val priceEuro: Int,
    val district: String,
    val location: String,
    val rooms: Double,
    val areaSqm: Double,
    val imageUrl: String?,
    val listingUrl: String,
    val isJobcenterSuitable: Boolean,
    val isWohngeldEligible: Boolean,
    val isWbsRequired: Boolean,
    val isFavorite: Boolean,
    val updatedAtEpochMillis: Long,
    val lastSeenAtEpochMillis: Long = updatedAtEpochMillis,
    val isActive: Boolean = true,
    val lifecycleStatus: ListingLifecycleStatus = ListingLifecycleStatus.ACTIVE,
)
