package com.sinop.sist.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Double.formatCurrency(currencyCode: String = "TRY"): String {
    val locale = when (currencyCode) {
        "USD" -> Locale.US
        "EUR" -> Locale.GERMANY
        else -> Locale("tr", "TR")
    }
    val symbol = when (currencyCode) {
        "USD" -> "$"
        "EUR" -> "€"
        else -> "₺"
    }
    val formatter = NumberFormat.getCurrencyInstance(locale)
    formatter.currency = java.util.Currency.getInstance(currencyCode)
    return try {
        formatter.format(this)
    } catch (e: Exception) {
        "$symbol${"%.2f".format(this)}"
    }
}

fun Double.formatPercent(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${"%.2f".format(this)}%"
}

fun LocalDateTime.formatDateTime(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("tr", "TR")))
}

fun LocalDateTime.formatDate(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("tr", "TR")))
}

fun LocalDate.formatShort(): String {
    return this.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("tr", "TR")))
}
