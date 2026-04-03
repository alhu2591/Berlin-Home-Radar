package com.berlin.homeradar.domain.model

data class SyncInfo(
    val lastSuccessfulSyncMillis: Long? = null,
    val lastAttemptMillis: Long? = null,
    val lastErrorMessage: String? = null,
    val isSyncing: Boolean = false,
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
)
