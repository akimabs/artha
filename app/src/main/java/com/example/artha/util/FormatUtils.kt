package com.example.artha.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
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
            val parsed = LocalDate.parse(input, DateTimeFormatter.ofPattern(format, Locale("id")))
            return parsed.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id")))
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
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id"))
    return currentDate.format(formatter)
}

fun extractDigitsOnly(input: String): Int {
    return input.replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0
}

@RequiresApi(Build.VERSION_CODES.O)
fun groupPengeluaranByMonthAndPocket(
    historyList: List<HistoryItemData>,
    pocketMap: Map<String, PocketData>
): Map<YearMonth, Map<String, Int>> {
    return historyList.groupBy {
        val date = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id")))
        YearMonth.of(date.year, date.month)
    }.mapValues { (_, listPerMonth) ->
        listPerMonth.groupBy { it.pocketId }
            .mapValues { (_, perPocket) -> perPocket.sumOf { it.amount } }
            .filterKeys { pocketMap.containsKey(it) } // keep only valid pockets
    }.toSortedMap(compareByDescending { it })
}