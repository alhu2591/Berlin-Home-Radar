package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.berlin.homeradar.data.local.entity.SourceMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceMetricDao {
    @Query("SELECT * FROM source_metric")
    fun observeAll(): Flow<List<SourceMetricEntity>>

    @Query("SELECT * FROM source_metric")
    suspend fun getAll(): List<SourceMetricEntity>

    @Upsert
    suspend fun upsertAll(metrics: List<SourceMetricEntity>)
}
