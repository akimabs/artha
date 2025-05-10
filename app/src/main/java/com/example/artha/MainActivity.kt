package com.example.artha

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val imageUri = intent?.takeIf {
            it.action == Intent.ACTION_SEND && it.type?.startsWith("image/") == true
        }?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        setContent {
            MaterialTheme {
                if (imageUri != null) {
                    ArthaApp(sharedImageUri = imageUri)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada gambar untuk diproses", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ArthaApp(sharedImageUri: Uri?) {
    val context = LocalContext.current
    var parsedResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    val formOffsetDp = with(LocalDensity.current) { (screenHeight * 0.6f).toDp() }

    LaunchedEffect(sharedImageUri) {
        sharedImageUri?.let { uri ->
            isLoading = true
            runOCR(context, uri) { text ->
                val cleanedText = normalizeAmountFormat(text) // <-- ini baru
                sendToLLM(cleanedText) { result ->
                    parsedResult = result
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        sharedImageUri?.let {
            Box {
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
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = formOffsetDp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                    ResultScreen(parsedJson = parsedResult, isLoading = isLoading)
                    Spacer(modifier = Modifier.height(30.dp))
                    BudgetCard(
                    title = "Bulanan | default",
                    spent = 1_000_000,
                    total = 5_000_000,
                    onSelectCategory = {
                        // Aksi ketika tombol kiri diklik (misal: tampilkan bottom sheet kategori)
                    },
                    onSubmit = {
                        // Aksi ketika tombol kanan ➡ diklik (misal: simpan data)
                    }
                )
            }
        }
    }
}

@Composable
fun ResultScreen(parsedJson: String, isLoading: Boolean) {
    var amount by remember { mutableStateOf(0) }
    val displayAmount by animateIntAsState(targetValue = amount, label = "amountAnim")
    var bank by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf<String?>(null) }

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
            date = parsed.optString("date")
        } catch (e: Exception) {
            bank = "Parsing Error"
            date = "-"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("IDR", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "%,d".format(displayAmount),
            style = MaterialTheme.typography.displayLarge
        )

        InfoRow(label = "Nama:", value = bank, isLoading = isLoading)
        InfoRow(label = "Tanggal Transaksi:", value = date, isLoading = isLoading)
        InfoRow(label = "Kategori:", value = "Others", isLoading = isLoading)
    }
}

@Composable
fun BudgetCard(
    title: String,
    spent: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onSelectCategory: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    // Animasi spent & progress
    var animateNow by remember { mutableStateOf(false) }

    val animatedSpent by animateIntAsState(
        targetValue = if (animateNow) spent else 0,
        animationSpec = tween(durationMillis = 800),
        label = "spentAnim"
    )

    val progressTarget = (spent.toFloat() / total).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (animateNow) progressTarget else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progressAnim"
    )

    // Start animation on launch
    LaunchedEffect(Unit) {
        animateNow = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Kiri: button kategori + progress + nominal
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelectCategory() }
        ) {
            // Background progress bar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D8CF2).copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D8CF2).copy(alpha = 0.35f))
            )

            // Foreground content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        Text(
                            text = "%,d".format(animatedSpent),
                            color = Color(0xFF0D8CF2),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = " / %,d".format(total),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // Kanan: Submit button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF5AB0F6))
                .clickable { onSubmit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?, isLoading: Boolean) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        if (isLoading) {
            ShimmerText()
        } else {
            Text(value ?: "-", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ShimmerText(modifier: Modifier = Modifier) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    Box(
        modifier = modifier
            .height(20.dp)
            .width(120.dp)
            .shimmer(shimmerInstance)
            .background(Color.Gray.copy(alpha = 0.2f))
    )
}

fun runOCR(context: Context, uri: Uri, onResult: (String) -> Unit) {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            onResult(visionText.text)
        }
        .addOnFailureListener {
            onResult("OCR failed: ${it.message}")
        }
}

fun normalizeAmountFormat(text: String): String {
    // 1. Ubah format Eropa (12.500,00) ➜ 12500.00
    val europeanNormalized = text.replace(Regex("(\\d)\\.(\\d{3})(,\\d{2})?")) {
        val noDot = it.value.replace(".", "")
        noDot.replace(",", ".")
    }

    // 2. Ubah format US (12,500.00) ➜ 12500.00
    return europeanNormalized.replace(Regex("(\\d),(\\d{3})(\\.\\d+)?")) {
        it.value.replace(",", "")
    }
}

fun sendToLLM(text: String, onResult: (String) -> Unit) {
    val apiKey = "AIzaSyC8Z8ZOyoUjS78GM_SfawaAVhh2KNIcrpE"
    val client = OkHttpClient()

    val prompt = """
     Dari teks berikut, ekstrak:
    - amount (jumlah uang dalam angka penuh, contoh: 75000 berarti tujuh puluh lima ribu rupiah),
    - date (tanggal transaksi),
    - bank (nama bank).

    Semua angka sudah dinormalisasi ke format angka internasional (contoh: 12500.00 berarti dua belas ribu lima ratus).

    Teks:\n$text
        Jawab dalam format JSON dengan kunci: amount, date, bank. Jangan sertakan ```json atau teks tambahan.
    """.trimIndent()

    val json = """
        {
          "contents": [{
            "parts": [{"text": "$prompt"}]
          }]
        }
    """.trimIndent()

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-thinking-exp-01-21:generateContent?key=$apiKey")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (responseBody != null) {
                try {
                    val parsed = JSONObject(responseBody)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val resultJson = JSONObject().put("text", parsed)
                    onResult(resultJson.toString())
                } catch (e: Exception) {
                    onResult("{\"text\": \"{\\\"error\\\": \\\"Parsing failed\\\"}\"}")
                }
            } else {
                onResult("{\"text\": \"{\\\"error\\\": \\\"Empty response\\\"}\"}")
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            onResult("{\"text\": \"{\\\"error\\\": \\\"${e.message}\\\"}\"}")
        }
    })
}