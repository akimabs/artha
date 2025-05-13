package com.example.artha.ui.screen.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.artha.ocr.runOCRSuspend
import com.example.artha.ocr.sendToLLMSuspend
import com.example.artha.util.LocalStorageManager
import com.example.artha.util.RateLimiter
import com.example.artha.util.getCurrentDateString
import com.example.artha.util.getCurrentTimeString
import com.example.artha.util.groupPengeluaranByMonthAndPocket
import com.example.artha.util.normalizeAmountFormat
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.YearMonth

data class ParsedTransaction(
    val title: String,
    val amount: Int,
    val category: String
)

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScannerReceipt(
    sharedImageUri: Uri?,
    onDone: () -> Unit
){
    val context = LocalContext.current
    var parsedResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var parsedTransaction by remember { mutableStateOf<ParsedTransaction?>(null) }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var hasError by remember { mutableStateOf(false) }

    var pocketList by remember {
        mutableStateOf(listOf<PocketData>())
    }

    val screenHeight = context.resources.displayMetrics.heightPixels
    val formOffsetDp = with(LocalDensity.current) { (screenHeight * 0.6f).toDp() }
    val coroutineScope = rememberCoroutineScope()
    val historyList = remember { mutableStateListOf<HistoryItemData>() }
    val llmRateLimiter = RateLimiter(10000L) // Maks 1 request / 10 detik
    val currentMonth = YearMonth.now()

    val monthlyBalanceMap by remember(historyList, pocketList) {
        derivedStateOf {
            val grouped = groupPengeluaranByMonthAndPocket(historyList, pocketList.associateBy { it.id })
            grouped[currentMonth] ?: emptyMap()
        }
    }

    fun processImage(uri: Uri) {
        coroutineScope.launch {
            isLoading = true
            hasError = false
            parsedResult = "" // Reset parsedResult to show loading state
            try {
                val rawText = runOCRSuspend(context, uri)
                val cleanedText = normalizeAmountFormat(rawText)
                llmRateLimiter.run {
                    val apiKey = LocalStorageManager.loadApiKey(context)
                    val result = sendToLLMSuspend(apiKey, cleanedText)
                    parsedResult = result
                }
            } catch (e: Exception) {
                Log.e("ArthaDebug", "❌ Error processing image: ${e.message}")
                parsedResult = "{\"text\":\"{\\\"error\\\":\\\"${e.message}\\\"}\"}"
                hasError = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect("first") {
        try {
            pocketList = LocalStorageManager.loadPockets(context)
            historyList.addAll(LocalStorageManager.loadHistory(context))
        } catch (e: Exception) {
            Log.e("ArthaDebug", "❌ Error loading pockets: ${e.message}")
            pocketList = emptyList()
        }
    }

    LaunchedEffect(sharedImageUri) {
        sharedImageUri?.let { uri ->
            currentImageUri = uri
            processImage(uri)
        }
    }

    LaunchedEffect(parsedResult) {
        try {
            if (parsedResult.isNotBlank()) {
                val outer = JSONObject(parsedResult)
                val innerText = outer.getString("text")
                val cleaned = innerText.replace(Regex("```json\\s*|```"), "").trim()
                val json = JSONObject(cleaned)

                parsedTransaction = ParsedTransaction(
                    title = json.getString("bank"),
                    amount = json.getString("amount").replace(Regex("[^\\d]"), "").toIntOrNull() ?: 0,
                    category = json.optString("category")
                )

                Log.d("ArthaDebug", "✅ ParsedTransaction: $parsedTransaction")
                hasError = false
            }
        } catch (e: Exception) {
            Log.e("ArthaDebug", "❌ Parsing error: ${e.message}")
            parsedTransaction = null
            hasError = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        currentImageUri?.let {
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
                ResultScreen(
                    parsedJson = parsedResult,
                    isLoading = isLoading
                )
                Spacer(modifier = Modifier.height(30.dp))
                    BudgetCard(
                        categoryOptions = pocketList,
                        title = parsedTransaction?.title.orEmpty(),
                        spent = (parsedTransaction?.amount ?: 0),
                        monthlyBalanceMap,
                        isLoading = isLoading,
                        onRetry = {
                            currentImageUri?.let { uri ->
                                processImage(uri)
                            }
                        },
                        onSubmit = { selectedPocketId, transactionTitle, amount ->
                            val updatedPockets = pocketList.map {
                                if (it.id == selectedPocketId) {
                                    it.copy(amount = it.amount + amount)
                                } else it
                            }
                            val newHistory = HistoryItemData(
                                title = transactionTitle,
                                amount = amount,
                                time = getCurrentTimeString(),
                                date = getCurrentDateString(),
                                pocketId = selectedPocketId,
                            )

                            pocketList = updatedPockets
                            historyList.add(0, newHistory) // prepend ke UI list

                            coroutineScope.launch {
                                try {
                                    LocalStorageManager.savePockets(context, updatedPockets)
                                    LocalStorageManager.appendHistory(context, newHistory)

                                    val reloadedHistory = LocalStorageManager.loadHistory(context)
                                    Log.d("ArthaDebug", "✅ Saved & Reloaded History: $reloadedHistory")
                                    onDone()
                                } catch (e: Exception) {
                                    Log.e("ArthaDebug", "❌ Failed saving data: ${e.message}")
                                }
                            }
                        }
                    )
            }
        }
    }
}