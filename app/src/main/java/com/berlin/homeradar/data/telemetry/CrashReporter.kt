package com.berlin.homeradar.data.telemetry

interface CrashReporter {
    fun log(message: String)
    fun recordNonFatal(throwable: Throwable, metadata: Map<String, String> = emptyMap())
}
