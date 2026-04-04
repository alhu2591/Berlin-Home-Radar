package com.berlin.homeradar.presentation.screen.listings

import com.berlin.homeradar.domain.filter.matchesFilter
import com.berlin.homeradar.domain.model.HousingListing
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingsFiltersTest {

    @Test
    fun hasActiveCriteria_returnsFalse_whenFiltersAreDefault() {
        assertFalse(ListingsFilters().hasActiveCriteria())
    }

    @Test
    fun hasActiveCriteria_returnsTrue_whenAnyFilterIsEnabled() {
        assertTrue(ListingsFilters(query = "mitte").hasActiveCriteria())
        assertTrue(ListingsFilters(showFavoritesOnly = true).hasActiveCriteria())
        assertTrue(ListingsFilters(selectedSourceIds = setOf("remote-json")).hasActiveCriteria())
    }

    @Test
    fun matches_appliesFavoritesAndSourceFiltersTogether() {
        val listing = HousingListing(
            id = 1L,
            source = "bundled-json",
            externalId = "sample-1",
            title = "Quiet apartment",
            priceEuro = 1100,
            district = "Mitte",
            location = "Berlin",
            rooms = 2.0,
            areaSqm = 60.0,
            imageUrl = null,
            listingUrl = "https://example.com/1",
            isJobcenterSuitable = false,
            isWohngeldEligible = true,
            isWbsRequired = false,
            isFavorite = true,
            updatedAtEpochMillis = 0L,
        )

        assertTrue(
            listing.matchesFilter(
                ListingsFilters(
                    showFavoritesOnly = true,
                    selectedSourceIds = setOf("bundled-json"),
                ),
            ),
        )
        assertFalse(
            listing.matchesFilter(
                ListingsFilters(
                    showFavoritesOnly = true,
                    selectedSourceIds = setOf("remote-json"),
                ),
            ),
        )
    }
}