package com.berlin.homeradar.data.source

import android.content.Context
import com.berlin.homeradar.data.source.model.RawListing
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledJsonListingSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : ListingSource {

    override val sourceId: String = "bundled-json"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetch(): List<RawListing> {
        val content = context.assets.open("listings_source_a.json")
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString<List<BundledJsonListing>>(content)
            .map {
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

@kotlinx.serialization.Serializable
private data class BundledJsonListing(
    val id: String,
    val title: String,
    val priceEuro: Int,
    val district: String,
    val location: String,
    val rooms: Double,
    val areaSqm: Double,
    val imageUrl: String? = null,
    val listingUrl: String,
    val jobcenterSuitable: Boolean,
    val wohngeldEligible: Boolean,
    val wbsRequired: Boolean,
)
