package com.berlin.homeradar.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteListingDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("priceEuro") val priceEuro: Int,
    @SerialName("district") val district: String,
    @SerialName("location") val location: String,
    @SerialName("rooms") val rooms: Double,
    @SerialName("areaSqm") val areaSqm: Double,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("listingUrl") val listingUrl: String,
    @SerialName("jobcenterSuitable") val jobcenterSuitable: Boolean = false,
    @SerialName("wohngeldEligible") val wohngeldEligible: Boolean = false,
    @SerialName("wbsRequired") val wbsRequired: Boolean = false,
)
