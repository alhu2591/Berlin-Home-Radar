package com.berlin.homeradar.domain.model

enum class ListingLifecycleStatus(val storageValue: String) {
    ACTIVE("active"),
    STALE("stale"),
    ARCHIVED("archived");

    companion object {
        fun fromStorage(value: String?): ListingLifecycleStatus =
            entries.firstOrNull { it.storageValue == value } ?: ACTIVE
    }
}
