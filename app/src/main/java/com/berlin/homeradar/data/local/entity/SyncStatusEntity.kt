package com.berlin.homeradar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey val key: String = "default",
    val lastSuccessfulSyncMillis: Long? = null,
    val lastAttemptMillis: Long? = null,
    val lastErrorMessage: String? = null,
    val isSyncing: Boolean = false,
)
