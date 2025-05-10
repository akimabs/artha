package com.example.artha.ocr

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

fun sendToLLM(text: String, onResult: (String) -> Unit) {
    val apiKey = "AIzaSyC8Z8ZOyoUjS78GM_SfawaAVhh2KNIcrpE"
    val client = OkHttpClient()

    val prompt = """
     Dari teks berikut, ekstrak:
    - amount (jumlah uang dalam angka penuh, contoh: 75000 berarti tujuh puluh lima ribu rupiah),
    - date (tanggal transaksi),
    - bank (nama bank).
    - category: deteksi jenis transaksi ini berdasarkan jumlah dan nama rekening.

    Semua angka sudah dinormalisasi ke format angka internasional (contoh: 12500.00 berarti dua belas ribu lima ratus).

    Teks:\n$text
        Gunakan bahasa Indonesia untuk jawaban. Jawab dalam format JSON dengan kunci: amount, date, bank, category. Jangan sertakan ```json atau teks tambahan.
    """.trimIndent()

    val json = """{"contents":[{"parts":[{"text":"$prompt"}]}]}"""
    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-thinking-exp-01-21:generateContent?key=$apiKey")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string() ?: return onResult("{\"text\":\"Empty response\"}")
            try {
                val parsed = JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                onResult(JSONObject().put("text", parsed).toString())
            } catch (e: Exception) {
                onResult("{\"text\":\"{\\\"error\\\":\\\"Parsing failed\\\"}\"}")
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            onResult("{\"text\":\"{\\\"error\\\":\\\"${e.message}\\\"}\"}")
        }
    })
}

// ocr/sendToLLM.kt
suspend fun sendToLLMSuspend(apiKey: String, text: String): String = suspendCancellableCoroutine { cont ->
    val client = OkHttpClient()

    val prompt = """
        Dari teks berikut, ekstrak:
        - amount (jumlah uang dalam angka penuh),
        - date (tanggal transaksi),
        - bank (nama bank),
        - category (jenis transaksi).
        
        Teks:\n$text

        Jawab dalam format JSON dengan kunci: amount, date, bank, category.
    """.trimIndent()

    val json = """{"contents":[{"parts":[{"text":"$prompt"}]}]}"""
    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-thinking-exp-01-21:generateContent?key=$apiKey")
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.resume("{\"text\":\"{\\\"error\\\":\\\"${e.message}\\\"}\"}", onCancellation = null)
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string() ?: return cont.resume("{\"text\":\"Empty response\"}", onCancellation = null)
            try {
                val parsed = JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                cont.resume(JSONObject().put("text", parsed).toString(), onCancellation = null)
            } catch (e: Exception) {
                cont.resume("{\"text\":\"{\\\"error\\\":\\\"Parsing failed\\\"}\"}", onCancellation = null)
            }
        }
    })
}
