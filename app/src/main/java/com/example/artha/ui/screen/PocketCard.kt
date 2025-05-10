package com.example.artha.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.artha.R

@Composable
fun PocketCard(
    title: String,
    amount: Int,
    targetAmount: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "highlightScale"
    )

    val walletIcon = painterResource(id = R.drawable.wallet)
    val formattedAmount = remember(amount) { "Rp%,d".format(amount) }

    val percentage = if (targetAmount != 0) {
        (amount * 100) / targetAmount
    } else {
        0
    }

    val isOverBudget = percentage > 100
    val progressColor = if (isOverBudget) Color.Red else Color(0xFF4CAF50)
    val percentTextColor = if (isOverBudget) Color.Red else Color.Black
    val progress = percentage.coerceIn(0, 100) / 100f

    Card(
        modifier = modifier
            .height(160.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Icon(
                    painter = walletIcon,
                    contentDescription = "Icon Dompet",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }

            Column {
                Text(formattedAmount, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = progressColor,
                        trackColor = Color.LightGray
                    )
                    Text(
                        "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        color = percentTextColor
                    )
                }

                if (isOverBudget) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Overbudget!",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
