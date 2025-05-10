package com.example.artha.ui.screen

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import com.example.artha.util.LocalStorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.input.TextFieldValue


@Composable
fun FilterBadge(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard() {
    var pocketList by remember { mutableStateOf(emptyList<PocketData>()) }
    val historyList = remember {
        mutableStateListOf<HistoryItemData>()
    }

    val totalPengeluaran = pocketList.sumOf { it.amount }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(0xFFFFFFCC.toInt()) }
    val listState = rememberLazyListState()
    var highlightedIndex by remember { mutableStateOf(-1) }
    var shouldAnimateNewItem by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedPocket by remember { mutableStateOf<String?>(null) }
    val apiKeyRegex = Regex("^AIza[0-9A-Za-z_-]{35}$")
    val isValid = apiKeyRegex.matches(apiKeyInput.text)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        pocketList = LocalStorageManager.loadPockets(context)
        historyList.addAll(LocalStorageManager.loadHistory(context))
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pengeluaran Bulan Ini", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pengaturan API",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showApiKeyDialog = true }
                    )
                }
                Text(
                    text = "Rp%,d".format(totalPengeluaran),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black
                )
            }
        }

        if (showApiKeyDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Setel API Key Gemini") },
                text = {
                    Column {
                        Text("Masukkan API Key kamu di bawah ini.")
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
                            label = { Text("API Key") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (isValid) {
                            coroutineScope.launch {
                                LocalStorageManager.saveApiKey(context, apiKeyInput.text)
                                showApiKeyDialog = false
                            }
                        }
                    }) {
                        Text("Simpan")
                    }
                },
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
                    Text("Saku Kamu", style = MaterialTheme.typography.titleLarge)
                    if(!pocketList.isEmpty()){
                        Text(
                            "+ Tambah Saku",
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
                            Text("Tambah Saku", color = Color(0xFF0D8CF2))
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
                            amount = pocket.amount,
                            targetAmount = pocket.targetAmount,
                            backgroundColor = pocket.backgroundColor,
                            isHighlighted = index == highlightedIndex,
                            modifier = Modifier
                                .padding(start = startPadding, end = endPadding)
                                .width(200.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Riwayat Pengeluaran", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterBadge(
                            label = "Semua",
                            selected = selectedPocket == null,
                            onClick = { selectedPocket = null }
                        )
                    }
                    items(pocketList) { pocket ->
                        FilterBadge(
                            label = pocket.title,
                            selected = selectedPocket == pocket.title,
                            onClick = { selectedPocket = pocket.title }
                        )
                    }
                }
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
                        Text("Belum ada transaksi tercatat", color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filteredHistory = if (selectedPocket != null) {
                            historyList.filter { it.pocket == selectedPocket }
                        } else {
                            historyList
                        }

                        filteredHistory.forEach {
                            HistoryItem(
                                title = it.title,
                                amount = it.amount,
                                time = it.time,
                                date = it.date
                            )
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
