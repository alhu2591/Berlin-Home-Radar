package com.berlin.homeradar.data.source

import android.content.Context
import com.berlin.homeradar.data.source.model.RawListing
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledHtmlListingSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : ListingSource {

    override val sourceId: String = "bundled-html"

    override suspend fun fetch(): List<RawListing> {
        val content = context.assets.open("listings_source_b.html")
            .bufferedReader()
            .use { it.readText() }

        val document = Jsoup.parse(content)
        return document.select(".listing").map { element ->
            RawListing(
                source = sourceId,
                externalId = element.attr("data-id"),
                title = element.selectFirst(".title")?.text().orEmpty(),
                priceEuro = element.selectFirst(".price")?.text()?.toIntOrNull() ?: 0,
                district = element.selectFirst(".district")?.text().orEmpty(),
                location = element.selectFirst(".location")?.text().orEmpty(),
                rooms = element.selectFirst(".rooms")?.text()?.toDoubleOrNull() ?: 0.0,
                areaSqm = element.selectFirst(".area")?.text()?.toDoubleOrNull() ?: 0.0,
                imageUrl = element.selectFirst("img")?.attr("src"),
                listingUrl = element.selectFirst(".url")?.attr("href").orEmpty(),
                isJobcenterSuitable = element.selectFirst(".jobcenter")?.text().toBoolean(),
                isWohngeldEligible = element.selectFirst(".wohngeld")?.text().toBoolean(),
                isWbsRequired = element.selectFirst(".wbs")?.text().toBoolean(),
            )
        }
    }
}
