package com.berlin.homeradar.data.telemetry

interface AnalyticsLogger {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}
