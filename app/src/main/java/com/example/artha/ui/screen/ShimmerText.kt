// ui/ShimmerText.kt
package com.example.artha.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerText(modifier: Modifier = Modifier) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    Box(
        modifier = modifier
            .height(20.dp)
            .width(120.dp)
            .shimmer(shimmerInstance)
            .background(
                color = Color.Gray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
    )
}
