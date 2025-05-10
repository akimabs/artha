package com.example.artha.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

fun runOCR(context: Context, uri: Uri, onResult: (String) -> Unit) {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText -> onResult(visionText.text) }
        .addOnFailureListener { onResult("OCR failed: ${it.message}") }
}

// Tambahan versi suspend
suspend fun runOCRSuspend(context: Context, uri: Uri): String = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText -> cont.resume(visionText.text, onCancellation = null) }
        .addOnFailureListener { cont.resume("OCR failed: ${it.message}", onCancellation = null) }
}
