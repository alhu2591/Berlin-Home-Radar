package com.berlin.homeradar.data.telemetry

import com.berlin.homeradar.data.config.FeatureFlags
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class RemoteTelemetryClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    fun sendAnalytics(name: String, params: Map<String, String>) {
        val endpoint = FeatureFlags.analyticsEndpoint ?: return
        if (!FeatureFlags.analyticsEnabled) return
        post(
            endpoint = endpoint,
            payload = TelemetryEnvelope(
                type = "analytics",
                name = name,
                params = params,
            ),
        )
    }

    fun sendCrash(message: String, throwable: Throwable?, metadata: Map<String, String>) {
        val endpoint = FeatureFlags.crashEndpoint ?: return
        if (!FeatureFlags.crashReportingEnabled) return
        post(
            endpoint = endpoint,
            payload = TelemetryEnvelope(
                type = "crash",
                name = message,
                params = metadata + mapOf(
                    "throwable" to (throwable?.javaClass?.name ?: "n/a"),
                    "stacktrace" to (throwable?.stackTraceToString() ?: ""),
                ),
            ),
        )
    }

    private fun post(endpoint: String, payload: TelemetryEnvelope) {
        val request = Request.Builder()
            .url(endpoint)
            .post(
                json.encodeToString(payload)
                    .toRequestBody("application/json".toMediaType()),
            )
            .build()

        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) = Unit
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.close()
            }
        })
    }
}

@kotlinx.serialization.Serializable
private data class TelemetryEnvelope(
    val type: String,
    val name: String,
    val params: Map<String, String>,
    val timestampMillis: Long = System.currentTimeMillis(),
)
