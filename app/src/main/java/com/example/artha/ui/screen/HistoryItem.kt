// ui/HistoryItem.kt
package com.example.artha.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
fun HistoryItem(
    title: String,
    pocketName: String,
    amount: Int,
    time: String,
    date: String,
    onDelete: (() -> Unit)
) {
    val deleteIcon = painterResource(id = R.drawable.delete_history)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(" • ", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(pocketName, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(end = 15.dp)
            ) {
                Text("Rp%,d".format(amount), style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    painter = deleteIcon,
                    contentDescription = "Hapus",
                    tint = Color.White.copy(0.6f),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(bottom = 3.dp, end = 3.dp)
                )
            }
        }
    }
}