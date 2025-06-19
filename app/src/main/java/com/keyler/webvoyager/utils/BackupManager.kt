package com.keyler.webvoyager.utils

import android.content.Context
import java.io.File

object BackupManager {

    private const val BACKUP_FILE_NAME = "bookmarks_backup.json"

    fun backupBookmarks(context: Context) {
        val bookmarks = BookmarkManager.getBookmarks(context)
        val json = BookmarkManager.gson.toJson(bookmarks)

        val file = File(context.filesDir, BACKUP_FILE_NAME)
        file.writeText(json)
    }

    fun restoreBookmarks(context: Context) {
        val file = File(context.filesDir, BACKUP_FILE_NAME)
        if (file.exists()) {
            val json = file.readText()
            val type = com.google.gson.reflect.TypeToken.getParameterized(
                List::class.java, com.keyler.webvoyager.model.FavoriteEntry::class.java
            ).type
            val restoredBookmarks: List<com.keyler.webvoyager.model.FavoriteEntry> = BookmarkManager.gson.fromJson(json, type)

            // Guardar los restaurados sobrescribiendo los actuales
            BookmarkManager.replaceAll(context, restoredBookmarks)
        }
    }

    fun deleteBackup(context: Context) {
        val file = File(context.filesDir, BACKUP_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
