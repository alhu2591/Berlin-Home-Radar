package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HousingListingDao {
    @Query(
        """
        SELECT * FROM housing_listing
        WHERE (:onlyFavorites = 0 OR isFavorite = 1)
          AND (:minRooms IS NULL OR rooms >= :minRooms)
          AND (:district IS NULL OR district = :district)
        ORDER BY updatedAtEpochMillis DESC, priceEuro ASC
        """
    )
    fun observeListings(
        onlyFavorites: Boolean,
        minRooms: Double?,
        district: String?,
    ): Flow<List<HousingListingEntity>>


    @Query("SELECT * FROM housing_listing ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    suspend fun getAll(): List<HousingListingEntity>

    @Query("SELECT * FROM housing_listing WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HousingListingEntity?

    @Query("SELECT * FROM housing_listing WHERE source = :source AND externalId = :externalId LIMIT 1")
    suspend fun getBySourceAndExternalId(source: String, externalId: String): HousingListingEntity?

    @Query("SELECT * FROM housing_listing WHERE fingerprint = :fingerprint ORDER BY updatedAtEpochMillis DESC LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): HousingListingEntity?

    @Upsert
    suspend fun upsert(entity: HousingListingEntity): Long

    @Query("UPDATE housing_listing SET isFavorite = NOT isFavorite WHERE id = :listingId")
    suspend fun toggleFavorite(listingId: Long)

    @Query("SELECT DISTINCT district FROM housing_listing ORDER BY district ASC")
    fun observeDistricts(): Flow<List<String>>
}
