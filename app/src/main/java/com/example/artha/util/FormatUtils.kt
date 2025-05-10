package com.example.artha.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
