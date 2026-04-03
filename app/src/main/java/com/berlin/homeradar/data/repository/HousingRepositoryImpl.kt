package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.data.source.ListingSourceRegistry
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.repository.HousingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HousingRepositoryImpl @Inject constructor(
    private val housingListingDao: HousingListingDao,
    private val syncStatusDao: SyncStatusDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listingSourceRegistry: ListingSourceRegistry,
    private val deduplicationPolicy: DeduplicationPolicy,
) : HousingRepository {

    override fun observeListings(
        onlyFavorites: Boolean,
        minRooms: Double?,
        district: String?,
    ): Flow<List<HousingListing>> {
        return housingListingDao.observeListings(
            onlyFavorites = onlyFavorites,
            minRooms = minRooms,
            district = district,
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeSyncInfo(): Flow<SyncInfo> {
        return combine(
            syncStatusDao.observe(),
            userPreferencesRepository.backgroundSyncEnabled,
            userPreferencesRepository.remoteSourceEnabled,
        ) { status, backgroundSyncEnabled, remoteSourceEnabled ->
            status.toDomain(
                backgroundSyncEnabled = backgroundSyncEnabled,
                remoteSourceEnabled = remoteSourceEnabled,
            )
        }
    }

    override suspend fun refreshListings(trigger: String): Result<Unit> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val currentStatus = syncStatusDao.observe().first() ?: SyncStatusEntity()

        syncStatusDao.upsert(
            currentStatus.copy(
                lastAttemptMillis = now,
                isSyncing = true,
                lastErrorMessage = null,
            )
        )

        runCatching {
            val listings = listingSourceRegistry.getEnabledSources().flatMap { it.fetch() }
            listings.forEach { incoming ->
                val directMatch = housingListingDao.getBySourceAndExternalId(
                    source = incoming.source,
                    externalId = incoming.externalId,
                )
                val fingerprintMatch = housingListingDao.getByFingerprint(
                    deduplicationPolicy.fingerprint(incoming)
                )
                val existing = directMatch ?: fingerprintMatch

                val entity = if (existing != null) {
                    deduplicationPolicy.merge(existing, incoming, now)
                } else {
                    deduplicationPolicy.toEntity(
                        raw = incoming,
                        existingId = null,
                        isFavorite = false,
                        now = now,
                    )
                }
                housingListingDao.upsert(entity)
            }

            syncStatusDao.upsert(
                currentStatus.copy(
                    lastSuccessfulSyncMillis = now,
                    lastAttemptMillis = now,
                    lastErrorMessage = null,
                    isSyncing = false,
                )
            )
        }.onFailure { throwable ->
            syncStatusDao.upsert(
                currentStatus.copy(
                    lastAttemptMillis = now,
                    lastErrorMessage = "[$trigger] ${throwable.message ?: "Unknown error"}",
                    isSyncing = false,
                )
            )
        }.map { Unit }
    }

    override suspend fun toggleFavorite(listingId: Long) {
        housingListingDao.toggleFavorite(listingId)
    }

    override suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        userPreferencesRepository.setBackgroundSyncEnabled(enabled)
    }

    override suspend fun setRemoteSourceEnabled(enabled: Boolean) {
        userPreferencesRepository.setRemoteSourceEnabled(enabled)
    }
}
