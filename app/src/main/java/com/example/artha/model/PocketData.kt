package com.example.artha.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class PocketData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Int,
    val backgroundColorInt: Int,
    val targetAmount: Int
) {
    val backgroundColor: Color
        get() = Color(backgroundColorInt)
}
