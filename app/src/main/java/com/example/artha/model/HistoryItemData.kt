package com.example.artha.model

import java.util.UUID

data class HistoryItemData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Int,
    val time: String,
    val date: String,
    val pocketId: String
)
