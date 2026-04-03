package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.source.model.RawListing
import org.junit.Assert.assertEquals
import org.junit.Test

class DeduplicationPolicyTest {
    private val policy = DeduplicationPolicy()

    @Test
    fun normalize_collapsesWhitespaceAndLowercases() {
        assertEquals("hello berlin", policy.normalize("  Hello   BERLIN "))
    }

    @Test
    fun fingerprint_usesNormalizedFields() {
        val raw = RawListing(
            source = "demo",
            externalId = "1",
            title = " Nice Flat ",
            priceEuro = 1200,
            district = " Mitte ",
            location = "Berlin",
            rooms = 2.5,
            areaSqm = 68.4,
            listingUrl = "https://example.com/1",
        )
        assertEquals("nice flat|1200|68|25|mitte", policy.fingerprint(raw))
    }
}
