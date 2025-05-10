package com.example.artha

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.core.view.WindowCompat
import com.example.artha.ui.screen.ArthaApp
import com.example.artha.ui.screen.HomeDashboard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val imageUri = intent?.takeIf {
            it.action == Intent.ACTION_SEND && it.type?.startsWith("image/") == true
        }?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        setContent {
            if (imageUri != null) {
                ArthaApp(sharedImageUri = imageUri)
            } else {
                HomeDashboard()
            }
        }
    }
}
