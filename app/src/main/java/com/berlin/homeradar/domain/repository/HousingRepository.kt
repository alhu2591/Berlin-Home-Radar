package com.berlin.homeradar.domain.repository

import com.berlin.homeradar.domain.model.HousingListing
import com.berlin.homeradar.domain.model.SyncInfo
import kotlinx.coroutines.flow.Flow

interface HousingRepository {
    fun observeListings(
        onlyFavorites: Boolean = false,
        minRooms: Double? = null,
        district: String? = null,
    ): Flow<List<HousingListing>>

    fun observeSyncInfo(): Flow<SyncInfo>

    suspend fun refreshListings(trigger: String): Result<Unit>

    suspend fun toggleFavorite(listingId: Long)
    suspend fun setBackgroundSyncEnabled(enabled: Boolean)
    suspend fun setRemoteSourceEnabled(enabled: Boolean)
}
