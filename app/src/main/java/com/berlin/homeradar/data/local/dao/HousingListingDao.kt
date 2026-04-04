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
        WHERE isActive = 1
          AND (:onlyFavorites = 0 OR isFavorite = 1)
          AND (:minRooms IS NULL OR rooms >= :minRooms)
          AND (:minArea IS NULL OR areaSqm >= :minArea)
          AND (:maxPrice IS NULL OR priceEuro <= :maxPrice)
          AND (:districtNormalized IS NULL OR districtNormalized = :districtNormalized)
          AND (:onlyJobcenter = 0 OR isJobcenterSuitable = 1)
          AND (:onlyWohngeld = 0 OR isWohngeldEligible = 1)
          AND (:onlyWbs = 0 OR isWbsRequired = 1)
          AND (:sourceFilteringDisabled = 1 OR source IN (:selectedSourceIds))
          AND (
            :queryNormalized = '' OR
            titleNormalized LIKE '%' || :queryNormalized || '%' OR
            districtNormalized LIKE '%' || :queryNormalized || '%' OR
            lower(location) LIKE '%' || :queryNormalized || '%' OR
            lower(source) LIKE '%' || :queryNormalized || '%'
          )
        ORDER BY updatedAtEpochMillis DESC, priceEuro ASC
        """
    )
    fun observeListings(
        onlyFavorites: Boolean,
        minRooms: Double?,
        minArea: Double?,
        maxPrice: Int?,
        districtNormalized: String?,
        queryNormalized: String,
        onlyJobcenter: Boolean,
        onlyWohngeld: Boolean,
        onlyWbs: Boolean,
        sourceFilteringDisabled: Boolean,
        selectedSourceIds: List<String>,
    ): Flow<List<HousingListingEntity>>

    @Query("SELECT * FROM housing_listing WHERE isActive = 1 ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    fun observeActiveListings(): Flow<List<HousingListingEntity>>

    @Query("SELECT * FROM housing_listing ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    suspend fun getAll(): List<HousingListingEntity>

    @Query("SELECT * FROM housing_listing WHERE isActive = 1 ORDER BY updatedAtEpochMillis DESC, priceEuro ASC")
    suspend fun getAllActive(): List<HousingListingEntity>

    @Query("SELECT * FROM housing_listing WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HousingListingEntity?

    @Query("SELECT * FROM housing_listing WHERE source = :source AND externalId = :externalId LIMIT 1")
    suspend fun getBySourceAndExternalId(source: String, externalId: String): HousingListingEntity?

    @Query("SELECT * FROM housing_listing WHERE fingerprint = :fingerprint ORDER BY updatedAtEpochMillis DESC LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): HousingListingEntity?

    @Upsert
    suspend fun upsert(entity: HousingListingEntity): Long

    @Upsert
    suspend fun upsertAll(entities: List<HousingListingEntity>)

    @Query("UPDATE housing_listing SET isFavorite = NOT isFavorite WHERE id = :listingId")
    suspend fun toggleFavorite(listingId: Long)

    @Query("SELECT DISTINCT district FROM housing_listing WHERE isActive = 1 ORDER BY district ASC")
    fun observeDistricts(): Flow<List<String>>
}
