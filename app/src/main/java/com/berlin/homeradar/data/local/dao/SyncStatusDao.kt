package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {
    @Query("SELECT * FROM sync_status WHERE key = 'default' LIMIT 1")
    fun observe(): Flow<SyncStatusEntity?>

    @Query("SELECT * FROM sync_status WHERE key = 'default' LIMIT 1")
    suspend fun get(): SyncStatusEntity?

    @Upsert
    suspend fun upsert(entity: SyncStatusEntity)
}
