package com.example.artha

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import com.example.artha.ui.AppNavigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        var imageUri = intent?.takeIf {
            it.action == Intent.ACTION_SEND && it.type?.startsWith("image/") == true
        }?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

        setContent {
            AppNavigation(
                sharedImageUri = imageUri,
                clearIntentImage = {
                    intent.removeExtra(Intent.EXTRA_STREAM)
                    imageUri = null
                }
            )
        }
    }
}
