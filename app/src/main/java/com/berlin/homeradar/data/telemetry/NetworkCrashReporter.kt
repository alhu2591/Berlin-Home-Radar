package com.berlin.homeradar.data.telemetry

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkCrashReporter @Inject constructor(
    private val remoteTelemetryClient: RemoteTelemetryClient,
) : CrashReporter {
    override fun log(message: String) {
        Log.d("BerlinHomeRadarCrash", message)
        remoteTelemetryClient.sendCrash(message, null, emptyMap())
    }

    override fun recordNonFatal(throwable: Throwable, metadata: Map<String, String>) {
        val context = if (metadata.isEmpty()) "" else metadata.entries.joinToString(prefix = " [", postfix = "]") { "${it.key}=${it.value}" }
        Log.e("BerlinHomeRadarCrash", "Non-fatal${context}: ${throwable.message}", throwable)
        remoteTelemetryClient.sendCrash(
            message = throwable.message ?: throwable.javaClass.simpleName,
            throwable = throwable,
            metadata = metadata,
        )
    }
}
