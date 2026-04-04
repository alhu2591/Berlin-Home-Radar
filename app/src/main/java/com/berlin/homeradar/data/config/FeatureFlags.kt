package com.berlin.homeradar.data.config

object FeatureFlags {
    private val defaultDisabledSourceIds: Set<String> = emptySet()
    private val defaultWebViewJavaScriptAllowedHosts: Set<String> = setOf(
        "www.immobilienscout24.de",
        "www.kleinanzeigen.de",
        "www.vonovia.de",
    )

    @Volatile
    private var remoteConfig: RemoteRuntimeConfigPayload = RemoteRuntimeConfigPayload(
        disabledSourceIds = defaultDisabledSourceIds,
        webViewJavaScriptAllowedHosts = defaultWebViewJavaScriptAllowedHosts,
        analyticsEnabled = false,
        analyticsEndpoint = null,
        crashReportingEnabled = false,
        crashEndpoint = null,
    )

    val disabledSourceIds: Set<String>
        get() = remoteConfig.disabledSourceIds.ifEmpty { defaultDisabledSourceIds }

    val webViewJavaScriptAllowedHosts: Set<String>
        get() = remoteConfig.webViewJavaScriptAllowedHosts.ifEmpty { defaultWebViewJavaScriptAllowedHosts }

    val analyticsEnabled: Boolean
        get() = remoteConfig.analyticsEnabled && !remoteConfig.analyticsEndpoint.isNullOrBlank()

    val analyticsEndpoint: String?
        get() = remoteConfig.analyticsEndpoint?.takeIf { it.isNotBlank() }

    val crashReportingEnabled: Boolean
        get() = remoteConfig.crashReportingEnabled && !remoteConfig.crashEndpoint.isNullOrBlank()

    val crashEndpoint: String?
        get() = remoteConfig.crashEndpoint?.takeIf { it.isNotBlank() }

    fun updateFromRemote(payload: RemoteRuntimeConfigPayload) {
        remoteConfig = payload.copy(
            disabledSourceIds = payload.disabledSourceIds.ifEmpty { defaultDisabledSourceIds },
            webViewJavaScriptAllowedHosts = payload.webViewJavaScriptAllowedHosts.ifEmpty { defaultWebViewJavaScriptAllowedHosts },
        )
    }

    fun isSourceEnabled(sourceId: String): Boolean = sourceId !in disabledSourceIds
}
