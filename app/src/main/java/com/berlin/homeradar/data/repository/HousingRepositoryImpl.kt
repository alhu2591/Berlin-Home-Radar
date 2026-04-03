package com.berlin.homeradar.data.repository

import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import com.berlin.homeradar.data.local.mapper.toDomain
import com.berlin.homeradar.data.preferences.UserPreferencesRepository
import com.berlin.homeradar.data.source.ListingSourceRegistry
import com.berlin.homeradar.data.source.SourceCatalog
import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import com.berlin.homeradar.domain.repository.HousingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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
            userPreferencesRepository.appSettings,
        ) { status, settings ->
            status.toDomain(
                backgroundSyncEnabled = settings.backgroundSyncEnabled,
                remoteSourceEnabled = settings.remoteSourceEnabled,
            ).copy(
                syncInterval = settings.syncInterval,
                appLanguage = settings.language,
                themeMode = settings.themeMode,
                enabledSourceIds = settings.enabledSourceIds,
                customSources = settings.customSources,
                sourceOrder = settings.sourceOrder,
            )
        }
    }

    override fun observeSavedSearches(): Flow<List<SavedSearch>> = userPreferencesRepository.savedSearches

    override fun getKnownSources(): List<SourceDefinition> = SourceCatalog.all

    override suspend fun getListingById(id: Long): HousingListing? = withContext(Dispatchers.IO) {
        housingListingDao.getById(id)?.toDomain()
    }

    override suspend fun refreshListings(trigger: String): Result<Unit> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val currentStatus = syncStatusDao.observeOnce() ?: SyncStatusEntity()

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
                    lastAttemptMillis = now,
                    lastSuccessfulSyncMillis = now,
                    isSyncing = false,
                    lastErrorMessage = null,
                )
            )
        }.onFailure { throwable ->
            syncStatusDao.upsert(
                currentStatus.copy(
                    lastAttemptMillis = now,
                    isSyncing = false,
                    lastErrorMessage = throwable.message ?: "Refresh failed",
                )
            )
        }
    }

    override suspend fun toggleFavorite(listingId: Long) {
        housingListingDao.toggleFavorite(listingId)
    }

    override suspend fun setBackgroundSyncEnabled(enabled: Boolean) = userPreferencesRepository.setBackgroundSyncEnabled(enabled)
    override suspend fun setRemoteSourceEnabled(enabled: Boolean) = userPreferencesRepository.setRemoteSourceEnabled(enabled)
    override suspend fun setAppLanguage(language: AppLanguage) = userPreferencesRepository.setAppLanguage(language)
    override suspend fun setThemeMode(themeMode: ThemeMode) = userPreferencesRepository.setThemeMode(themeMode)
    override suspend fun setSyncInterval(option: SyncIntervalOption) = userPreferencesRepository.setSyncInterval(option)
    override suspend fun setSourceEnabled(sourceId: String, enabled: Boolean) = userPreferencesRepository.setSourceEnabled(sourceId, enabled)
    override suspend fun setAllSupportedSourcesEnabled(enabled: Boolean) = userPreferencesRepository.setAllSupportedSourcesEnabled(enabled)
    override suspend fun moveSource(sourceId: String, moveUp: Boolean) = userPreferencesRepository.moveSource(sourceId, moveUp)
    override suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String) =
        userPreferencesRepository.addCustomSource(displayName, websiteUrl, description)

    override suspend fun removeCustomSource(sourceId: String) = userPreferencesRepository.removeCustomSource(sourceId)

    override suspend fun saveSearch(search: SavedSearch) = userPreferencesRepository.saveSearch(search)
    override suspend fun deleteSavedSearch(searchId: String) = userPreferencesRepository.deleteSavedSearch(searchId)
    override suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean) =
        userPreferencesRepository.updateSavedSearchAlerts(searchId, enabled)

    override suspend fun exportBackupJson(): String = userPreferencesRepository.exportBackupJson()
    override suspend fun importBackupJson(json: String): Result<Unit> = userPreferencesRepository.replaceAllFromBackup(json)
}
