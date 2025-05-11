package com.example.artha.ui.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.artha.model.PocketData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCard(
    categoryOptions: List<PocketData>,
    title: String,
    spent: Int,
    category: String,
    modifier: Modifier = Modifier,
    onSubmit: (selectedPocket: String, title: String, amount: Int) -> Unit
) {
    var animateNow by remember { mutableStateOf(false) }
    var selectedPocketTitle by remember { mutableStateOf("") }
    var budgetTarget by remember { mutableStateOf(0) }
    var budgetCurrent by remember { mutableStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPocketId by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val totalBudgetAfterSpend = budgetCurrent + spent
    val safeTarget = budgetTarget.takeIf { it > 0 } ?: 1
    val isOverBudget = totalBudgetAfterSpend > budgetTarget

    val progressColor = if (isOverBudget) Color.Red else Color(0xFF0D8CF2)
    val textColor = if (isOverBudget) Color.Red else Color.Black

    val progressRatio = totalBudgetAfterSpend.toFloat() / safeTarget
    val animatedSpent by animateIntAsState(
        targetValue = if (animateNow) totalBudgetAfterSpend else 0,
        animationSpec = tween(durationMillis = 800),
        label = "spentAnim"
    )

    val clampedProgress = progressRatio.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (animateNow) clampedProgress else 0f,
        animationSpec = tween(
            durationMillis = (800 + (progressRatio * 400)).toInt().coerceAtMost(2000),
            easing = LinearOutSlowInEasing
        ),
        label = "progressAnim"
    )


    LaunchedEffect(Unit) { animateNow = true }

    LaunchedEffect(categoryOptions) {
        if (categoryOptions.isNotEmpty()) {
            val first = categoryOptions.first()
            selectedPocketTitle = first.title
            selectedPocketId = first.id
            budgetTarget = first.targetAmount
            budgetCurrent = first.amount
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(
                text = "Pilih Saku",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            categoryOptions.forEach { response ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPocketTitle = response.title
                            budgetTarget = response.targetAmount
                            budgetCurrent = response.amount
                            selectedPocketId = response.id
                            showBottomSheet = false
                        },
                    headlineContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(response.title)
                            val isOver = response.amount > response.targetAmount
                            Row {
                                Text(
                                    text = "Rp %,d/".format(response.amount),
                                    color = if (isOver) Color.Red else Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Rp %,d".format(response.targetAmount),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
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
                    .clickable { showBottomSheet = true }
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val fullWidth = constraints.maxWidth.toFloat()
                    val animatedWidth = (animatedProgress.coerceIn(0f, 1f) * fullWidth).dp

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFF0D8CF2).copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(animatedWidth)
                            .background(progressColor.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row {
                        Text(
                            text = selectedPocketTitle,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (isOverBudget) {
                            Text(" • ", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Overbudget!",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Row {
                        Text(
                            text = "%,d".format(animatedSpent),
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(" / ", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "%,d".format(budgetTarget),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            val submitEnabled = spent > 0
            val submitColor = if (submitEnabled) Color(0xFF5AB0F6) else Color(0xFFB0BEC5)

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(submitColor)
                    .clickable {
                        if (submitEnabled) {
                            onSubmit(selectedPocketId, title, spent)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (submitEnabled) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                } else {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}