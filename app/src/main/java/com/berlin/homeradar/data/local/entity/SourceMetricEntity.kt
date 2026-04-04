package com.berlin.homeradar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source_metric")
data class SourceMetricEntity(
    @PrimaryKey val sourceId: String,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val lastAttemptMillis: Long? = null,
    val lastSuccessfulPullMillis: Long? = null,
    val lastItemCount: Int = 0,
    val averageItemCount: Double = 0.0,
    val averageDurationMillis: Double = 0.0,
    val consecutiveZeroItemPulls: Int = 0,
    val lastErrorMessage: String? = null,
)
