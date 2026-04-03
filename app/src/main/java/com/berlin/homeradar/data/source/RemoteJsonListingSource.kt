package com.berlin.homeradar.data.source

import com.berlin.homeradar.data.remote.api.RemoteListingsService
import com.berlin.homeradar.data.source.model.RawListing
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteJsonListingSource @Inject constructor(
    private val service: RemoteListingsService,
) : ListingSource {

    override val sourceId: String = "remote-json"

    override suspend fun fetch(): List<RawListing> {
        return service.getListings().map {
            RawListing(
                source = sourceId,
                externalId = it.id,
                title = it.title,
                priceEuro = it.priceEuro,
                district = it.district,
                location = it.location,
                rooms = it.rooms,
                areaSqm = it.areaSqm,
                imageUrl = it.imageUrl,
                listingUrl = it.listingUrl,
                isJobcenterSuitable = it.jobcenterSuitable,
                isWohngeldEligible = it.wohngeldEligible,
                isWbsRequired = it.wbsRequired,
            )
        }
    }
}
