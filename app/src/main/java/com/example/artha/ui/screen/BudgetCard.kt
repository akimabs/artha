package com.example.artha.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BudgetCard(
    title: String,
    spent: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onSelectCategory: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    var animateNow by remember { mutableStateOf(false) }

    val animatedSpent by animateIntAsState(
        targetValue = if (animateNow) spent else 0,
        animationSpec = tween(durationMillis = 800),
        label = "spentAnim"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val interactionSource2 = remember { MutableInteractionSource() }
    val progressTarget = (spent.toFloat() / total).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (animateNow) progressTarget else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progressAnim"
    )

    LaunchedEffect(Unit) { animateNow = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = true),
                    onClick = onSelectCategory
                ),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D8CF2).copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D8CF2).copy(alpha = 0.35f))
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                    Row {
                        Text("%,d".format(animatedSpent), color = Color(0xFF0D8CF2), style = MaterialTheme.typography.titleMedium)
                        Text(" / %,d".format(total), color = Color.Black, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF5AB0F6))
                .clickable(
                    interactionSource = interactionSource2,
                    indication = rememberRipple(bounded = true),
                    onClick = onSubmit
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}