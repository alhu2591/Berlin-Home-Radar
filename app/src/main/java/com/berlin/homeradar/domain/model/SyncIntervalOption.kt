package com.berlin.homeradar.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncIntervalOption(val storageValue: String, val minutes: Long?) {
    MANUAL("manual", null),
    MINUTES_15("15m", 15),
    MINUTES_30("30m", 30),
    HOUR_1("1h", 60),
    HOURS_3("3h", 180);

    companion object {
        fun fromStorage(value: String?): SyncIntervalOption = entries.firstOrNull { it.storageValue == value } ?: MINUTES_15
    }
}
