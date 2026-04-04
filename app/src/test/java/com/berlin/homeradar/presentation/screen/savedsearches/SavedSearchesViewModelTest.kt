package com.berlin.homeradar.presentation.screen.savedsearches

import com.berlin.homeradar.domain.model.ListingFilterPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SavedSearchesViewModelTest {
    @Test
    fun newSearch_startsWithAlertsDisabled() {
        val search = SavedSearchesViewModel.newSearch(
            name = "Mitte under 1300",
            filters = ListingFilterPreset(maxPrice = 1300),
        )

        assertEquals("Mitte under 1300", search.name)
        assertFalse(search.alertsEnabled)
        assertEquals(1300, search.filters.maxPrice)
    }
}
