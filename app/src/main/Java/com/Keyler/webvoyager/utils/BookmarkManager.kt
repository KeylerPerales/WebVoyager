package com.keyler.webvoyager.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.keyler.webvoyager.model.Bookmark

object BookmarkManager {
    private const val PREF_NAME = "bookmarks"
    private const val KEY_BOOKMARKS = "bookmark_list"
    private val gson = Gson()

    fun saveBookmark(context: Context, bookmark: Bookmark) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getBookmarks(context).toMutableList()
        current.add(bookmark)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(current)).apply()
    }

    fun getBookmarks(context: Context): List<Bookmark> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val type = object : TypeToken<List<Bookmark>>() {}.type
        return gson.fromJson(json, type)
    }
}
