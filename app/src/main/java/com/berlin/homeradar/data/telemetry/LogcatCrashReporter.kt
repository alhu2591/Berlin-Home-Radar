package com.berlin.homeradar.data.telemetry

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogcatCrashReporter @Inject constructor() : CrashReporter {
    override fun log(message: String) {
        Log.d("BerlinHomeRadarCrash", message)
    }

    override fun recordNonFatal(throwable: Throwable, metadata: Map<String, String>) {
        val context = if (metadata.isEmpty()) "" else metadata.entries.joinToString(prefix = " [", postfix = "]") { "${it.key}=${it.value}" }
        Log.e("BerlinHomeRadarCrash", "Non-fatal${context}: ${throwable.message}", throwable)
    }
}
