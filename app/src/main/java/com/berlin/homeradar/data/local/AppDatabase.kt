package com.berlin.homeradar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.berlin.homeradar.data.local.dao.HousingListingDao
import com.berlin.homeradar.data.local.dao.SourceMetricDao
import com.berlin.homeradar.data.local.dao.SyncStatusDao
import com.berlin.homeradar.data.local.entity.HousingListingEntity
import com.berlin.homeradar.data.local.entity.SourceMetricEntity
import com.berlin.homeradar.data.local.entity.SyncStatusEntity

@Database(
    entities = [HousingListingEntity::class, SyncStatusEntity::class, SourceMetricEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun housingListingDao(): HousingListingDao
    abstract fun syncStatusDao(): SyncStatusDao
    abstract fun sourceMetricDao(): SourceMetricDao
}
