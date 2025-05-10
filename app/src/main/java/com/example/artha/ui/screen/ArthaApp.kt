// ui/ArthaApp.kt
package com.example.artha.ui.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import com.example.artha.ocr.runOCR
import com.example.artha.ocr.sendToLLM
import com.example.artha.util.normalizeAmountFormat
import org.json.JSONObject

@Composable
fun ArthaApp(sharedImageUri: Uri?) {
    val context = LocalContext.current
    var parsedResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var pocketList by remember {
        mutableStateOf(
            listOf(
                PocketData("Jajan Harian", 120_000, Color(0xFFD1E8FF), 30),
            )
        )
    }
    var historyList by remember { mutableStateOf(listOf<HistoryItemData>()) }

    val screenHeight = context.resources.displayMetrics.heightPixels
    val formOffsetDp = with(LocalDensity.current) { (screenHeight * 0.6f).toDp() }

    LaunchedEffect(sharedImageUri) {
        sharedImageUri?.let { uri ->
            isLoading = true
            runOCR(context, uri) { text ->
                val cleanedText = normalizeAmountFormat(text)
                sendToLLM(cleanedText) { result ->
                    parsedResult = result
                    isLoading = false

                    try {
                        val parsed = JSONObject(result).getString("text")
                        val obj = JSONObject(parsed.replace("```json", "").replace("```", ""))
                        val newAmount = obj.getString("amount").replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0
                        val newTitle = obj.optString("bank", "Tidak Diketahui")
                        val newDate = obj.optString("date", "-")

                        historyList = historyList + HistoryItemData(
                            title = newTitle,
                            amount = newAmount,
                            time = "-",
                            date = newDate
                        )

                        pocketList = pocketList.toMutableList().apply {
                            val first = firstOrNull()
                            if (first != null) {
                                val updated = first.copy(amount = first.amount + newAmount)
                                this[0] = updated
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        sharedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(formOffsetDp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(formOffsetDp)
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = formOffsetDp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ResultScreen(parsedJson = parsedResult, isLoading = isLoading)
                Spacer(modifier = Modifier.height(30.dp))
                BudgetCard(
                    title = pocketList.firstOrNull()?.title ?: "Bulanan",
                    spent = pocketList.firstOrNull()?.amount ?: 0,
                    total = 5_000_000
                )
            }
        }
    }
}