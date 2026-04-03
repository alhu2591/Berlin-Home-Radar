package com.berlin.homeradar.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncIntervalOptionTest {
    @Test
    fun fromStorage_returnsExpectedValue() {
        assertEquals(SyncIntervalOption.HOUR_1, SyncIntervalOption.fromStorage("1h"))
    }

    @Test
    fun fromStorage_fallsBackToDefault() {
        assertEquals(SyncIntervalOption.MINUTES_15, SyncIntervalOption.fromStorage("unknown"))
    }
}
