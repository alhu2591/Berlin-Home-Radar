package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SourceMetricDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.data.source.ListingSource
import com.berlin.homeradar.data.source.ListingSourceRegistry
import com.berlin.homeradar.data.source.model.RawListing
import com.berlin.homeradar.data.telemetry.AnalyticsLogger
import com.berlin.homeradar.data.telemetry.CrashReporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.onBlocking
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class HousingRepositoryImplTest {
    private val housingListingDao: HousingListingDao = mock()
    private val sourceMetricDao: SourceMetricDao = mock()
    private val syncStatusDao: SyncStatusDao = mock()
    private val userPreferencesRepository: UserPreferencesRepository = mock()
    private val listingSourceRegistry: ListingSourceRegistry = mock()
    private val analyticsLogger: AnalyticsLogger = mock()
    private val crashReporter: CrashReporter = mock()
    private val deduplicationPolicy = DeduplicationPolicy()

    private lateinit var repository: HousingRepositoryImpl

    @Before
    fun setUp() {
        repository = HousingRepositoryImpl(
            housingListingDao = housingListingDao,
            sourceMetricDao = sourceMetricDao,
            syncStatusDao = syncStatusDao,
            userPreferencesRepository = userPreferencesRepository,
            listingSourceRegistry = listingSourceRegistry,
            deduplicationPolicy = deduplicationPolicy,
            analyticsLogger = analyticsLogger,
            crashReporter = crashReporter,
        )
    }

    @Test
    fun refreshListings_batchesMergedEntitiesWithoutPerItemLookups() = runTest {
        val existing = HousingListingEntity(
            id = 42L,
            source = "bundled-json",
            externalId = "listing-1",
            title = "Old title",
            titleNormalized = "old title",
            priceEuro = 900,
            district = "Mitte",
            districtNormalized = "mitte",
            location = "Berlin",
            rooms = 2.0,
            areaSqm = 60.0,
            imageUrl = "https://example.com/old.jpg",
            listingUrl = "https://example.com/old",
            isJobcenterSuitable = false,
            isWohngeldEligible = false,
            isWbsRequired = false,
            isFavorite = true,
            fingerprint = "updated flat|1200|65|25|mitte",
            updatedAtEpochMillis = 1L,
            lastSeenAtEpochMillis = 1L,
        )
        val sourceListings = listOf(
            rawListing(source = "bundled-json", externalId = "listing-1"),
            rawListing(source = "remote-json", externalId = "listing-2"),
        )

        onBlocking { syncStatusDao.get() } doReturn SyncStatusEntity()
        onBlocking { housingListingDao.getAll() } doReturn listOf(existing)
        onBlocking { sourceMetricDao.getAll() } doReturn emptyList()
        onBlocking { listingSourceRegistry.getEnabledSources() } doReturn listOf(
            FakeListingSource(sourceId = "bundled-json", listings = listOf(sourceListings[0])),
        )

        val result = repository.refreshListings(trigger = "manual")

        assertTrue(result.isSuccess)
        verify(housingListingDao, never()).getBySourceAndExternalId(any(), any())
        verify(housingListingDao, never()).getByFingerprint(any())

        val entityCaptor = argumentCaptor<List<HousingListingEntity>>()
        verify(housingListingDao).upsertAll(entityCaptor.capture())

        val mergedEntities = entityCaptor.firstValue
        assertEquals(1, mergedEntities.size)
        val merged = mergedEntities.single()
        assertEquals(existing.id, merged.id)
        assertTrue(merged.isFavorite)
        assertEquals("Updated flat", merged.title)
        assertEquals(1200, merged.priceEuro)
        assertTrue(merged.isActive)
    }

    @Test
    fun refreshListings_returnsFailureWhenAllSourcesFail() = runTest {
        onBlocking { syncStatusDao.get() } doReturn SyncStatusEntity()
        onBlocking { listingSourceRegistry.getEnabledSources() } doReturn listOf(
            FailingListingSource("bundled-json"),
            FailingListingSource("remote-json"),
        )

        val result = repository.refreshListings(trigger = "worker")

        assertTrue(result.isFailure)
        verify(housingListingDao, never()).getAll()
        verify(crashReporter).recordNonFatal(any(), any())
        verify(syncStatusDao).upsert(any())
    }

    private fun rawListing(source: String, externalId: String) = RawListing(
        source = source,
        externalId = externalId,
        title = "Updated flat",
        priceEuro = 1200,
        district = "Mitte",
        location = "Berlin",
        rooms = 2.5,
        areaSqm = 65.0,
        imageUrl = "https://example.com/new.jpg",
        listingUrl = "https://example.com/$externalId",
        isJobcenterSuitable = true,
        isWohngeldEligible = true,
        isWbsRequired = false,
    )
}

private class FakeListingSource(
    override val sourceId: String,
    private val listings: List<RawListing>,
) : ListingSource {
    override suspend fun fetch(): List<RawListing> = listings
}

private class FailingListingSource(
    override val sourceId: String,
) : ListingSource {
    override suspend fun fetch(): List<RawListing> {
        error("boom")
    }
}
