package com.berlin.homeradar.domain.repository

import com.berlin.homeradar.domain.model.AppLanguage
import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SavedSearch
import com.berlin.homeradar.domain.model.SourceDefinition
import com.berlin.homeradar.domain.model.SyncInfo
import com.berlin.homeradar.domain.model.SyncIntervalOption
import com.berlin.homeradar.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface HousingRepository {
    fun observeListings(
        onlyFavorites: Boolean = false,
        minRooms: Double? = null,
        district: String? = null,
    ): Flow<List<HousingListing>>

    fun observeSyncInfo(): Flow<SyncInfo>
    fun observeSavedSearches(): Flow<List<SavedSearch>>
    fun getKnownSources(): List<SourceDefinition>

    suspend fun getListingById(id: Long): HousingListing?
    suspend fun refreshListings(trigger: String): Result<Unit>

    suspend fun toggleFavorite(listingId: Long)
    suspend fun setBackgroundSyncEnabled(enabled: Boolean)
    suspend fun setRemoteSourceEnabled(enabled: Boolean)
    suspend fun setAppLanguage(language: AppLanguage)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setSyncInterval(option: SyncIntervalOption)
    suspend fun setSourceEnabled(sourceId: String, enabled: Boolean)
    suspend fun setAllSupportedSourcesEnabled(enabled: Boolean)
    suspend fun moveSource(sourceId: String, moveUp: Boolean)
    suspend fun addCustomSource(displayName: String, websiteUrl: String, description: String)
    suspend fun removeCustomSource(sourceId: String)
    suspend fun saveSearch(search: SavedSearch)
    suspend fun deleteSavedSearch(searchId: String)
    suspend fun updateSavedSearchAlerts(searchId: String, enabled: Boolean)
    suspend fun exportBackupJson(): String
    suspend fun importBackupJson(json: String): Result<Unit>
}
