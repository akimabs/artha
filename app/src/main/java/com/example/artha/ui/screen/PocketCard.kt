// ui/PocketCard.kt
package com.example.artha.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.artha.R

@Composable
fun PocketCard(
    title: String,
    amount: Int,
    percentage: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Icon(
                    painter = painterResource(id = R.drawable.wallet),
                    contentDescription = "Icon Dompet",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }

            Column {
                Text("Rp%,d".format(amount), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray
                    )
                    Text("$percentage%", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}