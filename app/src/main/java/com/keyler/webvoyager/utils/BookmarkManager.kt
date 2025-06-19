package com.keyler.webvoyager.utils

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.keyler.webvoyager.model.FavoriteEntry

object BookmarkManager {
    private const val PREF_NAME = "bookmarks"
    private const val KEY_BOOKMARKS = "bookmark_list"
    val gson = Gson()

    fun saveBookmark(context: Context, bookmark: FavoriteEntry) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getBookmarks(context).toMutableList()
        current.add(bookmark)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(current)).apply()
    }

    fun getBookmarks(context: Context): List<FavoriteEntry> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun deleteBookmark(context: Context, bookmark: FavoriteEntry) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getBookmarks(context).toMutableList()
        current.remove(bookmark)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(current)).apply()
    }

    fun editBookmark(context: Context, oldBookmark: FavoriteEntry, newTitle: String, newUrl: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getBookmarks(context).toMutableList()
        val index = current.indexOfFirst { it.title == oldBookmark.title && it.url == oldBookmark.url }

        if (index != -1) {
            val updatedBookmark = FavoriteEntry(
                title = newTitle,
                url = newUrl,
                dateAdded = current[index].dateAdded // Mantenemos la fecha original
            )
            current[index] = updatedBookmark
            prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(current)).apply()
        }
    }

    fun searchBookmarks(context: Context, query: String): List<FavoriteEntry> {
        return getBookmarks(context).filter {
            it.title.contains(query, ignoreCase = true) || it.url.contains(query, ignoreCase = true)
        }
    }

    fun sortBookmarks(context: Context, byTitle: Boolean = true): List<FavoriteEntry> {
        val bookmarks = getBookmarks(context)
        return if (byTitle) {
            bookmarks.sortedBy { it.title.lowercase() }
        } else {
            bookmarks // Puedes implementar orden por fecha si agregamos timestamp
        }
    }

    fun exportBookmarks(context: Context): String {
        val bookmarks = getBookmarks(context)
        return gson.toJson(bookmarks)
    }

    fun importBookmarks(context: Context, json: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKMARKS, json).apply()
    }

    fun shareBookmarks(context: Context) {
        val json = exportBookmarks(context)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/json"
        intent.putExtra(Intent.EXTRA_TEXT, json)
        context.startActivity(Intent.createChooser(intent, "Share bookmarks"))
    }

    fun replaceAll(context: Context, newList: List<FavoriteEntry>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(newList)).apply()
    }
}
