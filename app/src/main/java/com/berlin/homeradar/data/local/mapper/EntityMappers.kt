package com.berlin.homeradar.data.local.mapper

import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SyncInfo

fun HousingListingEntity.toDomain(): HousingListing = HousingListing(
    id = id,
    source = source,
    externalId = externalId,
    title = title,
    priceEuro = priceEuro,
    district = district,
    location = location,
    rooms = rooms,
    areaSqm = areaSqm,
    imageUrl = imageUrl,
    listingUrl = listingUrl,
    isJobcenterSuitable = isJobcenterSuitable,
    isWohngeldEligible = isWohngeldEligible,
    isWbsRequired = isWbsRequired,
    isFavorite = isFavorite,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

fun SyncStatusEntity?.toDomain(
    backgroundSyncEnabled: Boolean,
    remoteSourceEnabled: Boolean,
): SyncInfo = SyncInfo(
    lastSuccessfulSyncMillis = this?.lastSuccessfulSyncMillis,
    lastAttemptMillis = this?.lastAttemptMillis,
    lastErrorMessage = this?.lastErrorMessage,
    isSyncing = this?.isSyncing ?: false,
    backgroundSyncEnabled = backgroundSyncEnabled,
    remoteSourceEnabled = remoteSourceEnabled,
)
