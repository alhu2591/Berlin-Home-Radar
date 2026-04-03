package com.berlin.homeradar.data.source.model

data class RawListing(
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
)
