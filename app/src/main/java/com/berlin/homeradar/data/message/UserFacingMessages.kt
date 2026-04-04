package com.berlin.homeradar.data.message

object UserFacingMessages {
    const val SECURE_CONNECTION_FAILED = "Secure connection failed for one or more sources. Open the source in your browser or try again later."
    const val NO_INTERNET = "No internet connection. Check your network and try again."
    const val REFRESH_FAILED_GENERIC = "Refresh failed. Please try again later."

    const val SOURCE_WEBVIEW = "WebView-assisted source. Open externally or add a dedicated adapter later."
    const val SOURCE_HTML_CATALOG = "Catalog-only HTML source. Add adapter before automated sync."
    const val SOURCE_API_METADATA = "API source metadata only. Provide adapter or endpoint."
    const val SOURCE_CATALOG_ONLY = "Catalog source only."
    const val SOURCE_TESTING = "Testing source adapter…"
    const val SOURCE_ADAPTER_HEALTHY = "Source adapter is healthy."
    const val SOURCE_TEST_FAILED = "Source test failed"
    const val SOURCE_ADAPTER_MISSING = "Adapter missing"

    fun sourceFailure(sourceName: String, detail: String): String = "$sourceName: $detail"
}
