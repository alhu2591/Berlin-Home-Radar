package com.berlin.homeradar.domain.model

data class SyncInfo(
    val lastSuccessfulSyncMillis: Long? = null,
    val lastAttemptMillis: Long? = null,
    val lastErrorMessage: String? = null,
    val isSyncing: Boolean = false,
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
    val syncInterval: SyncIntervalOption = SyncIntervalOption.MINUTES_15,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val enabledSourceIds: Set<String> = emptySet(),
    val customSources: List<SourceDefinition> = emptyList(),
    val sourceOrder: List<String> = emptyList(),
)
