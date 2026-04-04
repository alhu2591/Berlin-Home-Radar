package com.berlin.homeradar.data.config

import kotlinx.serialization.Serializable

@Serializable
data class RemoteRuntimeConfigPayload(
    val disabledSourceIds: Set<String> = emptySet(),
    val webViewJavaScriptAllowedHosts: Set<String> = emptySet(),
    val analyticsEnabled: Boolean = true,
    val analyticsEndpoint: String? = null,
    val crashReportingEnabled: Boolean = true,
    val crashEndpoint: String? = null,
)
