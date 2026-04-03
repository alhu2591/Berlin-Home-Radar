package com.berlin.homeradar.presentation.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun formatPrice(priceEuro: Int): String = "€$priceEuro"

fun formatArea(areaSqm: Double): String = String.format(Locale.GERMANY, "%.0f m²", areaSqm)

fun formatRooms(rooms: Double): String = String.format(Locale.GERMANY, "%.1f rooms", rooms)

fun formatTimestamp(timestampMillis: Long?): String {
    if (timestampMillis == null) return "Never"
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(timestampMillis))
}
