package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {
    @Query("SELECT * FROM sync_status WHERE key = 'default'")
    fun observe(): Flow<SyncStatusEntity?>

    @Query("SELECT * FROM sync_status WHERE id = 1 LIMIT 1")
    suspend fun observeOnce(): SyncStatusEntity?

    @Upsert
    suspend fun upsert(entity: SyncStatusEntity)
}
