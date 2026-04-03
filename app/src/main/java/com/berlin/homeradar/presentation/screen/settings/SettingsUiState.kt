package com.berlin.homeradar.presentation.screen.settings

data class SettingsUiState(
    val backgroundSyncEnabled: Boolean = true,
    val remoteSourceEnabled: Boolean = false,
    val lastSuccessfulSyncText: String = "Never",
    val lastAttemptText: String = "Never",
    val lastErrorMessage: String? = null,
)
