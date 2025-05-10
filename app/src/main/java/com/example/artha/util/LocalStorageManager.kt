package com.example.artha.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.artha.model.HistoryItemData
import com.example.artha.model.PocketData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("artha_storage")

object LocalStorageManager {
    private val gson = Gson()
    private val POCKET_LIST_KEY = stringPreferencesKey("pocket_list")
    private val HISTORY_LIST_KEY = stringPreferencesKey("history_list")
    private val API_KEY = stringPreferencesKey("api_key")

    suspend fun saveApiKey(context: Context, key: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }

    suspend fun loadApiKey(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[API_KEY] ?: ""
    }

    suspend fun savePockets(context: Context, pockets: List<PocketData>) {
        val json = gson.toJson(pockets)
        context.dataStore.edit { it[POCKET_LIST_KEY] = json }
    }

    suspend fun loadPockets(context: Context): List<PocketData> {
        val prefs = context.dataStore.data.first()
        val json = prefs[POCKET_LIST_KEY] ?: return emptyList()
        val type = object : TypeToken<List<PocketData>>() {}.type
        return gson.fromJson(json, type)
    }

    suspend fun saveHistory(context: Context, history: List<HistoryItemData>) {
        val json = gson.toJson(history)
        context.dataStore.edit { it[HISTORY_LIST_KEY] = json }
    }

    suspend fun appendHistory(context: Context, newItem: HistoryItemData) {
        val currentList = loadHistory(context).toMutableList()
        currentList.add(0, newItem) // prepend; change to add() if you want append at end
        saveHistory(context, currentList)
    }

    suspend fun loadHistory(context: Context): List<HistoryItemData> {
        val prefs = context.dataStore.data.first()
        val json = prefs[HISTORY_LIST_KEY] ?: return emptyList()
        val type = object : TypeToken<List<HistoryItemData>>() {}.type
        return gson.fromJson(json, type)
    }
}