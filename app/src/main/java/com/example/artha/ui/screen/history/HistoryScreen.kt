package com.example.artha.ui.screen.history

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.artha.R
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import com.example.artha.util.LocalStorageManager
import com.example.artha.util.groupPengeluaranByMonthAndPocket
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryItemData>>(emptyList()) }
    var pocketList by remember { mutableStateOf<List<PocketData>>(emptyList()) }

    LaunchedEffect(Unit) {
        historyList = LocalStorageManager.loadHistory(context)
        pocketList = LocalStorageManager.loadPockets(context)
    }

    val pocketMap = pocketList.associateBy { it.id }
    val monthlyByPocket = groupPengeluaranByMonthAndPocket(historyList, pocketMap)
    val displayFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id"))
    var expandedMonth by remember { mutableStateOf<YearMonth?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.monthly_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE2ECEB)
                )
            )
        },
        containerColor = Color(0xFFF0F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (monthlyByPocket.isEmpty()) {
                Text(stringResource(R.string.no_transaction_data), color = Color.Gray)
            } else {
                monthlyByPocket.forEach { (yearMonth, pocketSummary) ->
                    val isExpanded = expandedMonth == yearMonth

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple(bounded = true),
                                    onClick = {
                                        expandedMonth = if (isExpanded) null else yearMonth
                                    }
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    yearMonth.format(displayFormatter),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Rp%,d".format(pocketSummary.values.sum()),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = stringResource(R.string.toggle),
                                        tint = Color.Gray
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(Modifier.height(8.dp))
                                    pocketSummary.forEach { (pocketId, amount) ->
                                        val pocket = pocketMap[pocketId]
                                        if (pocket != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(pocket.title, style = MaterialTheme.typography.bodyMedium)
                                                Text("Rp%,d".format(amount), style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
