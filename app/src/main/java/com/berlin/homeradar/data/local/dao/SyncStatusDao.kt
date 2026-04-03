package com.berlin.homeradar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.berlin.homeradar.data.local.entity.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {

    @Query("SELECT * FROM sync_status LIMIT 1")
    fun observe(): Flow<SyncStatusEntity?>

    @Query("SELECT * FROM sync_status LIMIT 1")
    suspend fun get(): SyncStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SyncStatusEntity)
}
