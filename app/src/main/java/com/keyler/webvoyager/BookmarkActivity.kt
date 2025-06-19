package com.keyler.webvoyager

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keyler.webvoyager.model.FavoriteEntry
import com.keyler.webvoyager.utils.BookmarkManager
import com.keyler.webvoyager.utils.BackupManager

class BookmarkActivity : AppCompatActivity() {

    private lateinit var adapter: FavoritesAdapter
    private lateinit var originalBookmarks: MutableList<FavoriteEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        title = "Favorites"

        // 1. Carga y orden inicial alfabético por título
        originalBookmarks = BookmarkManager
            .getBookmarks(this)
            .sortedBy { it.title.lowercase() }
            .toMutableList()

        // 2. Setup RecyclerView & Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        adapter = FavoritesAdapter(originalBookmarks, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 3. Botón de ordenamiento (alfabético ascendente por título)
        findViewById<Button>(R.id.buttonSort).setOnClickListener {
            // Cada vez que ordenas, actualizas la lista mostrada
            val sorted = originalBookmarks.sortedBy { it.title.lowercase() }
            adapter.updateData(sorted.toMutableList())
        }

        // 4. SearchView para filtrar en tiempo real
        findViewById<SearchView>(R.id.searchViewFavorites).setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    val filtered = if (!newText.isNullOrBlank()) {
                        originalBookmarks.filter {
                            it.title.contains(newText, ignoreCase = true) ||
                                    it.url.contains(newText, ignoreCase = true)
                        }
                    } else {
                        originalBookmarks
                    }
                    adapter.updateData(filtered.toMutableList())
                    return true
                }
            }
        )

        // 5. Backup
        findViewById<Button>(R.id.buttonBackup).setOnClickListener {
            BackupManager.backupBookmarks(this)
            Toast.makeText(this, "Backup completed", Toast.LENGTH_SHORT).show()
        }

        // 6. Restore
        findViewById<Button>(R.id.buttonRestore).setOnClickListener {
            BackupManager.restoreBookmarks(this)
            Toast.makeText(this, "Restore completed", Toast.LENGTH_SHORT).show()
            reloadFavorites()
        }
    }

    private fun reloadFavorites() {
        // Recarga desde el motor, reordena y refresca adapter
        val bookmarks = BookmarkManager
            .getBookmarks(this)
            .sortedBy { it.title.lowercase() }
            .toMutableList()
        originalBookmarks = bookmarks
        adapter.updateData(bookmarks)
    }
}