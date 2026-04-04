package com.berlin.homeradar.domain.model

data class RemoteConfigInfo(
    val endpointUrl: String? = null,
    val isEnabled: Boolean = false,
    val lastSuccessfulFetchMillis: Long? = null,
    val lastErrorMessage: String? = null,
)
