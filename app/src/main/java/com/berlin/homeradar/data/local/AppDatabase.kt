package com.berlin.homeradar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SyncStatusEntity

@Database(
    entities = [HousingListingEntity::class, SyncStatusEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun housingListingDao(): HousingListingDao
    abstract fun syncStatusDao(): SyncStatusDao
}
