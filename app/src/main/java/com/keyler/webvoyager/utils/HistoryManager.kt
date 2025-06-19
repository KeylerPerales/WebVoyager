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
		saveHistory(context, current)
	}

	fun getHistory(context: Context): List<HistoryEntry> {
		val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
		val type = object : TypeToken<List<HistoryEntry>>() {}.type
		return gson.fromJson(json, type)
	}

	fun deleteEntry(context: Context, entry: HistoryEntry) {
		val historyList = getHistory(context).toMutableList()
		historyList.removeAll { it.url == entry.url && it.title == entry.title }
		saveHistory(context, historyList)
	}

	fun clearHistory(context: Context) {
		saveHistory(context, emptyList())
	}

	private fun saveHistory(context: Context, historyList: List<HistoryEntry>) {
		val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		prefs.edit().putString(KEY_HISTORY, gson.toJson(historyList)).apply()
	}
}