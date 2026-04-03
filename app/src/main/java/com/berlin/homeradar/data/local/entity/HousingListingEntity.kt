package com.berlin.homeradar.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "housing_listing",
    indices = [
        Index(value = ["source", "externalId"], unique = true),
        Index(value = ["fingerprint"], unique = false),
    ],
)
data class HousingListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val source: String,
    val externalId: String,
    val title: String,
    val titleNormalized: String,
    val priceEuro: Int,
    val district: String,
    val districtNormalized: String,
    val location: String,
    val rooms: Double,
    val areaSqm: Double,
    val imageUrl: String?,
    val listingUrl: String,
    val isJobcenterSuitable: Boolean,
    val isWohngeldEligible: Boolean,
    val isWbsRequired: Boolean,
    val isFavorite: Boolean = false,
    val fingerprint: String,
    val updatedAtEpochMillis: Long,
)
