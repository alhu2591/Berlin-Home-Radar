package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val tag: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    GERMAN("de"),
    ARABIC("ar");

    companion object {
        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: SYSTEM
    }
}
