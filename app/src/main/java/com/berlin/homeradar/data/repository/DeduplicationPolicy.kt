package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.domain.model.ListingLifecycleStatus
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class DeduplicationPolicy @Inject constructor() {

    fun fingerprint(listing: RawListing): String {
        val title = normalize(listing.title)
        val district = normalize(listing.district)
        val area = listing.areaSqm.roundToInt()
        val rooms = (listing.rooms * 10.0).roundToInt()
        return "$title|${listing.priceEuro}|$area|$rooms|$district"
    }

    fun normalize(value: String): String = value
        .trim()
        .lowercase(Locale.GERMANY)
        .replace(Regex("\s+"), " ")

    fun merge(existing: HousingListingEntity, incoming: RawListing, now: Long): HousingListingEntity {
        return existing.copy(
            title = incoming.title,
            titleNormalized = normalize(incoming.title),
            priceEuro = incoming.priceEuro,
            district = incoming.district,
            districtNormalized = normalize(incoming.district),
            location = incoming.location,
            rooms = incoming.rooms,
            areaSqm = incoming.areaSqm,
            imageUrl = incoming.imageUrl ?: existing.imageUrl,
            listingUrl = incoming.listingUrl,
            isJobcenterSuitable = incoming.isJobcenterSuitable,
            isWohngeldEligible = incoming.isWohngeldEligible,
            isWbsRequired = incoming.isWbsRequired,
            fingerprint = fingerprint(incoming),
            updatedAtEpochMillis = now,
            lastSeenAtEpochMillis = now,
            isActive = true,
            lifecycleStatus = ListingLifecycleStatus.ACTIVE.storageValue,
        )
    }

    fun toEntity(raw: RawListing, existingId: Long?, isFavorite: Boolean, now: Long): HousingListingEntity {
        return HousingListingEntity(
            id = existingId ?: 0L,
            source = raw.source,
            externalId = raw.externalId,
            title = raw.title,
            titleNormalized = normalize(raw.title),
            priceEuro = raw.priceEuro,
            district = raw.district,
            districtNormalized = normalize(raw.district),
            location = raw.location,
            rooms = raw.rooms,
            areaSqm = raw.areaSqm,
            imageUrl = raw.imageUrl,
            listingUrl = raw.listingUrl,
            isJobcenterSuitable = raw.isJobcenterSuitable,
            isWohngeldEligible = raw.isWohngeldEligible,
            isWbsRequired = raw.isWbsRequired,
            isFavorite = isFavorite,
            fingerprint = fingerprint(raw),
            updatedAtEpochMillis = now,
            lastSeenAtEpochMillis = now,
            isActive = true,
            lifecycleStatus = ListingLifecycleStatus.ACTIVE.storageValue,
        )
    }
}
