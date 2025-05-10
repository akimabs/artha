package com.example.artha

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.example.artha.ui.screen.ArthaApp
import com.example.artha.ui.screen.HomeDashboard

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val imageUri = intent?.takeIf {
            it.action == Intent.ACTION_SEND && it.type?.startsWith("image/") == true
        }?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        setContent {
            var showHome by remember { mutableStateOf(imageUri == null) }

            if (showHome) {
                HomeDashboard()
            } else {
                ArthaApp(
                    sharedImageUri = imageUri,
                    onDone = { showHome = true }
                )
            }
        }
    }
}
