package com.example.artha.ui.screen.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.artha.util.formatReadableDate
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ResultScreen(parsedJson: String, isLoading: Boolean) {
    var amount by remember { mutableIntStateOf(0) }
    val displayAmount by animateIntAsState(targetValue = amount, label = "amountAnim")
    var bank by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<String?>(null) }

    val isParsed = !isLoading && parsedJson.isNotBlank()

    LaunchedEffect(parsedJson) {
        if (!isParsed) return@LaunchedEffect
        try {
            val outer = JSONObject(parsedJson)
            val innerText = outer.getString("text")
            val cleaned = innerText.replace(Regex("```json\\s*|```"), "").trim()
            val parsed = JSONObject(cleaned)

            amount = parsed.getString("amount").replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0
            bank = parsed.optString("bank")
            val rawDate = parsed.optString("date")
            date = formatReadableDate(rawDate)
            category = parsed.optString("category")
        } catch (e: Exception) {
            bank = "Parsing Error"
            date = "-"
            category = "-"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Indonesia Rupiah", style = MaterialTheme.typography.titleMedium)
            Text("%,d".format(displayAmount), style = MaterialTheme.typography.displayLarge)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(label = "Nama:", value = bank, isLoading = isLoading)
            InfoRow(label = "Tanggal Transaksi:", value = date, isLoading = isLoading)
            InfoRow(label = "Kategori:", value = category, isLoading = isLoading)
        }
    }
}