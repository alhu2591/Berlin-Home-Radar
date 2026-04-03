package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SourceType(val storageValue: String) {
    API("api"),
    HTML("html"),
    WEBVIEW("webview"),
    CATALOG("catalog");

    companion object {
        fun fromStorage(value: String?): SourceType = entries.firstOrNull { it.storageValue == value } ?: CATALOG
    }
}
