package com.example.artha.ui.screen.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import com.example.artha.util.LocalStorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.util.fastFirstOrNull
import com.example.artha.R
import com.example.artha.util.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun FilterBadge(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF0D8CF2) else Color(0xFFE0E0E0))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(onNavigateToHistory: () -> Unit = {}) {
    var pocketList by remember { mutableStateOf<List<PocketData>>(emptyList()) }
    var historyList by remember { mutableStateOf<List<HistoryItemData>>(emptyList()) }

    val historyIcon = painterResource(id = R.drawable.history)

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(0xFFFFFFCC.toInt()) }
    val listState = rememberLazyListState()
    var highlightedIndex by remember { mutableStateOf(-1) }
    var shouldAnimateNewItem by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPocket by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentMonth = remember { YearMonth.now() }
    val perPocketAmount = remember(historyList) {
        groupPengeluaranByMonthAndPocket(historyList, pocketList.associateBy { it.id })
    }
    val totalThisMonth = perPocketAmount[currentMonth]?.values?.sum() ?: 0

    LaunchedEffect(Unit) {
        historyList = LocalStorageManager.loadHistory(context)
        pocketList = LocalStorageManager.loadPockets(context)
        var storedApiKey = LocalStorageManager.loadApiKey(context)
        apiKeyInput = TextFieldValue(storedApiKey)

        if (storedApiKey.isBlank()) {
            showApiKeyDialog = true
        }
        Log.d("ArthaDebug", "History Loaded: ${historyList.joinToString("\n")}")
    }

    LaunchedEffect(pocketList.size, shouldAnimateNewItem) {
        if (shouldAnimateNewItem) {
            delay(100)
            listState.animateScrollToItem(pocketList.lastIndex)
            highlightedIndex = pocketList.lastIndex
            delay(600)
            highlightedIndex = -1
            shouldAnimateNewItem = false
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = Color(selectedColor)
        ) {
            AddPocketBottomSheet(
                selectedColor = selectedColor,
                onDismiss = { showBottomSheet = false },
                onColorChange = { selectedColor = it },
                onAddPocket = { newPocket ->
                pocketList = pocketList + newPocket
                shouldAnimateNewItem = true

                coroutineScope.launch {
                    LocalStorageManager.savePockets(context, pocketList)
                }
            }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            color = Color(0xFFE2ECEB),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(42.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.monthly_expenses),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.set_gemini_api_key),
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showApiKeyDialog = true }
                    )
                }
                Text(
                    text = "Rp%,d".format(totalThisMonth),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true),
                            onClick = { onNavigateToHistory() }
                        )
                        .padding(horizontal = 12.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = historyIcon,
                        contentDescription = "History Pocket",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        stringResource(R.string.history),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        ),
                    )
                }
            }
        }

        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                title = { Text(stringResource(R.string.set_gemini_api_key)) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.enter_api_key))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedLabelColor = Color(0xFF5AB0F6),
                                focusedBorderColor = Color(0xFF5AB0F6),
                                cursorColor = Color.Black
                            ),
                            singleLine = true,
                            label = { Text(stringResource(R.string.api_key)) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.no_api_key),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.create_now),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF5AB0F6),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://makersuite.google.com/app/apikey")
                                    )
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (apiKeyInput.text.isNotBlank()) {
                            coroutineScope.launch {
                                LocalStorageManager.saveApiKey(context, apiKeyInput.text)
                                showApiKeyDialog = false
                            }
                        }
                    }) {
                        Text(stringResource(R.string.save), color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showApiKeyDialog = false
                            coroutineScope.launch {
                                delay(350)
                                (context as? Activity)?.finish()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.Gray)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Column() {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.your_pockets), style = MaterialTheme.typography.titleLarge)
                    if(!pocketList.isEmpty()){
                        Text(
                            stringResource(R.string.add_pocket),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0D8CF2),
                            modifier = Modifier.clickable { showBottomSheet = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (pocketList.isEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEEEEE))
                            .clickable { showBottomSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Saku", tint = Color(0xFF0D8CF2))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_pocket), color = Color(0xFF0D8CF2))
                        }
                    }
                }
            } else {
                LazyRow(state = listState) {
                    itemsIndexed(pocketList) { index, pocket ->
                        val horizontalPadding = 12.dp
                        val startPadding = if (index == 0) 20.dp else horizontalPadding
                        val endPadding = if (index == pocketList.lastIndex) 20.dp else 0.dp
                        PocketCard(
                            title = pocket.title,
                            amount = perPocketAmount[currentMonth]?.get(pocket.id) ?: 0,
                            targetAmount = pocket.targetAmount,
                            backgroundColor = pocket.backgroundColor,
                            isHighlighted = index == highlightedIndex,
                            modifier = Modifier
                                .padding(start = startPadding, end = endPadding)
                                .width(200.dp),
                            onDelete = {
                                coroutineScope.launch {
                                    LocalStorageManager.deletePocket(context, pocket)
                                    pocketList = LocalStorageManager.loadPockets(context)
                                    historyList = historyList.filterNot { it.pocketId == pocket.id }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(stringResource(R.string.expense_history), style = MaterialTheme.typography.titleLarge)
            }
            if (pocketList.isNotEmpty()) {
                LazyRow {
                    item {
                        FilterBadge(
                            label = stringResource(R.string.all),
                            selected = selectedPocket == null,
                            onClick = { selectedPocket = null },
                            modifier = Modifier.padding(start = 20.dp, end = 0.dp, top = 15.dp)
                        )
                    }
                    itemsIndexed(pocketList) { index, pocket ->
                        val startPadding = 10.dp
                        val endPadding = if (index == pocketList.lastIndex) 20.dp else 0.dp

                        FilterBadge(
                            label = pocket.title,
                            selected = selectedPocket == pocket.id,
                            onClick = { selectedPocket = pocket.id },
                            modifier = Modifier.padding(
                                start = startPadding,
                                end = endPadding,
                                top = 15.dp
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_transactions), color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filteredHistory = historyList.filter { item ->
                            val itemDate = try {
                                LocalDate.parse(item.date, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id")))
                            } catch (_: Exception) {
                                null
                            }
                            val isInCurrentMonth = itemDate?.let { YearMonth.from(it) == currentMonth } ?: false

                            (selectedPocket == null || item.pocketId == selectedPocket) && isInCurrentMonth
                        }

                        filteredHistory.forEach {
                            HistoryItem(
                                title = it.title,
                                amount = it.amount,
                                time = it.time,
                                date = it.date,
                                pocketName = pocketList.fastFirstOrNull { p -> p.id == it.pocketId }?.title ?: "-",
                                onDelete = {
                                    coroutineScope.launch {
                                        LocalStorageManager.deleteHistoryItem(context, it)
                                        historyList = historyList.filterNot { h -> h.id == it.id }
                                        pocketList = LocalStorageManager.loadPockets(context)
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
