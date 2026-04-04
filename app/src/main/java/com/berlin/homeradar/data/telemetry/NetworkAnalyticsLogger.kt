package com.berlin.homeradar.data.telemetry

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkAnalyticsLogger @Inject constructor(
    private val remoteTelemetryClient: RemoteTelemetryClient,
) : AnalyticsLogger {
    override fun logEvent(name: String, params: Map<String, String>) {
        Log.d("BerlinHomeRadarAnalytics", buildString {
            append(name)
            if (params.isNotEmpty()) {
                append(" -> ")
                append(params.entries.joinToString { "${it.key}=${it.value}" })
            }
        })
        remoteTelemetryClient.sendAnalytics(name, params)
    }
}
