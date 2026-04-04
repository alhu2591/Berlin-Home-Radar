package com.berlin.homeradar.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.berlin.homeradar.R
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun formatPrice(priceEuro: Int): String = formatPrice(LocalContext.current, priceEuro)

@Composable
fun formatArea(areaSqm: Double): String = formatArea(LocalContext.current, areaSqm)

@Composable
fun formatRooms(rooms: Double): String = formatRooms(LocalContext.current, rooms)

@Composable
fun formatTimestamp(timestampMillis: Long?): String = formatTimestamp(LocalContext.current, timestampMillis)

fun formatPrice(context: Context, priceEuro: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(resolveLocale(context)).apply {
        currency = Currency.getInstance("EUR")
        maximumFractionDigits = 0
    }
    return formatter.format(priceEuro)
}

fun formatArea(context: Context, areaSqm: Double): String {
    return context.getString(
        R.string.area_value,
        decimalFormatter(resolveLocale(context), maxFractionDigits = 1).format(areaSqm),
    )
}

fun formatRooms(context: Context, rooms: Double): String {
    return context.getString(
        R.string.rooms_value,
        decimalFormatter(resolveLocale(context), maxFractionDigits = 1).format(rooms),
    )
}

fun formatTimestamp(context: Context, timestampMillis: Long?): String {
    if (timestampMillis == null) return context.getString(R.string.never_label)
    val formatter = DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM,
        FormatStyle.SHORT,
    ).withLocale(resolveLocale(context))
    return formatter.format(
        Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()),
    )
}

private fun resolveLocale(context: Context): Locale {
    val locales = context.resources.configuration.locales
    return if (!locales.isEmpty) locales[0] else Locale.getDefault()
}

private fun decimalFormatter(locale: Locale, maxFractionDigits: Int): NumberFormat {
    return NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        this.maximumFractionDigits = maxFractionDigits
    }
}
