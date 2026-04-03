package com.berlin.homeradar.domain.model

data class AppSettings(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val syncInterval: SyncIntervalOption = SyncIntervalOption.MINUTES_15,
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
    val enabledSourceIds: Set<String> = emptySet(),
    val customSources: List<SourceDefinition> = emptyList(),
    val sourceOrder: List<String> = emptyList(),
    val savedSearches: List<SavedSearch> = emptyList(),
)
