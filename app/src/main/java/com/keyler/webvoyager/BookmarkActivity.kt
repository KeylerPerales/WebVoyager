package com.keyler.webvoyager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.keyler.webvoyager.utils.BookmarkManager

class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		title = "Favorites"
		
        val listView = ListView(this)
        setContentView(listView)  // Corrección aquí

        val bookmarks = BookmarkManager.getBookmarks(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bookmarks.map { it.title })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent()
            intent.putExtra("url", bookmarks[position].url)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}