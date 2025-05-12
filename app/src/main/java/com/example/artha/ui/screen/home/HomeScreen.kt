package com.example.artha.ui.screen.home

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    sharedImageUri: Uri?,
    onNavigateToHistory: () -> Unit,
    clearIntentImage: () -> Unit
) {
    var showHome by remember { mutableStateOf(sharedImageUri == null) }

    if (showHome) {
        HomeDashboard(onNavigateToHistory = onNavigateToHistory)
    } else {
        ScannerReceipt(
            sharedImageUri = sharedImageUri,
            onDone = {
                clearIntentImage()
                showHome = true
            }
        )
    }
}
