package com.keyler.webvoyager.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keyler.webvoyager.model.HistoryEntry

object HistoryManager {
    private const val PREF_NAME = "history"
    private const val KEY_HISTORY = "history_list"
    private val gson = Gson()

    fun saveEntry(context: Context, entry: HistoryEntry) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getHistory(context).toMutableList()
        current.add(0, entry) // agrega al inicio
        prefs.edit().putString(KEY_HISTORY, gson.toJson(current)).apply()
    }

    fun getHistory(context: Context): List<HistoryEntry> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().remove(KEY_HISTORY).apply()
    }
}