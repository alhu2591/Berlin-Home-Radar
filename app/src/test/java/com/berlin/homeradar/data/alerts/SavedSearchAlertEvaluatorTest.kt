package com.berlin.homeradar.data.alerts

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.domain.model.AppSettings
import com.berlin.homeradar.domain.model.ListingFilterPreset
import com.berlin.homeradar.domain.model.SavedSearch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.onBlocking
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class SavedSearchAlertEvaluatorTest {
    private val housingListingDao: HousingListingDao = mock()
    private val userPreferencesRepository: UserPreferencesRepository = mock()
    private val notificationManager: AppNotificationManager = mock()

    private val evaluator = SavedSearchAlertEvaluator(
        housingListingDao = housingListingDao,
        userPreferencesRepository = userPreferencesRepository,
        notificationManager = notificationManager,
    )

    @Test
    fun evaluateAndNotify_onlyNotifiesForNewMatches() = runTest {
        val savedSearch = SavedSearch(
            id = "family-homes",
            name = "Family homes",
            filters = ListingFilterPreset(minRooms = 2.0, maxPrice = 1500),
            alertsEnabled = true,
        )
        onBlocking { housingListingDao.getAllActive() } doReturn listOf(
            listing(id = 1L, title = "Seen result", priceEuro = 1200),
            listing(id = 2L, title = "Fresh result", priceEuro = 1300),
        )
        onBlocking { userPreferencesRepository.appSettingsSnapshot() } doReturn AppSettings(
            savedSearches = listOf(savedSearch),
        )
        onBlocking { userPreferencesRepository.getSeenAlertMatches() } doReturn mapOf(
            savedSearch.id to setOf(1L),
        )

        evaluator.evaluateAndNotify()

        verify(notificationManager).showSavedSearchAlert(
            searchName = eq(savedSearch.name),
            matchCount = eq(1),
            listing = any(),
        )
        verify(userPreferencesRepository).markSeenAlertMatches(
            mapOf(savedSearch.id to setOf(1L, 2L)),
        )
        verifyNoMoreInteractions(notificationManager)
    }

    private fun listing(id: Long, title: String, priceEuro: Int) = HousingListingEntity(
        id = id,
        source = "bundled-json",
        externalId = "ext-$id",
        title = title,
        titleNormalized = title.lowercase(),
        priceEuro = priceEuro,
        district = "Mitte",
        districtNormalized = "mitte",
        location = "Berlin",
        rooms = 3.0,
        areaSqm = 70.0,
        imageUrl = null,
        listingUrl = "https://example.com/$id",
        isJobcenterSuitable = true,
        isWohngeldEligible = true,
        isWbsRequired = false,
        isFavorite = false,
        fingerprint = "fp-$id",
        updatedAtEpochMillis = 100L,
        lastSeenAtEpochMillis = 100L,
    )
}
