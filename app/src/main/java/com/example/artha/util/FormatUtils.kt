package com.example.artha.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun normalizeAmountFormat(text: String): String {
    val european = text.replace(Regex("(\\d)\\.(\\d{3})(,\\d{2})?")) {
        it.value.replace(".", "").replace(",", ".")
    }
    return european.replace(Regex("(\\d),(\\d{3})(\\.\\d+)?")) {
        it.value.replace(",", "")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatReadableDate(input: String?): String {
    if (input.isNullOrBlank()) return "-"
    val formats = listOf(
        "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "yyyy/MM/dd"
    )
    for (format in formats) {
        try {
            val parsed = LocalDate.parse(input, DateTimeFormatter.ofPattern(format))
            return parsed.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        } catch (_: DateTimeParseException) {}
    }
    return input
}


@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentTimeString(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return currentTime.format(formatter) // Contoh output: "14:35"
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateString(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    return currentDate.format(formatter) // Contoh output: "10 Mei 2025"
}

fun extractDigitsOnly(input: String): Int {
    return input.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0
}

fun formatPercentage(value: Int): String {
    val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("#,###", symbols)
    return formatter.format(value)
}