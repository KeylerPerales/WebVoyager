package com.keyler.webvoyager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.keyler.webvoyager.R
import com.keyler.webvoyager.model.HistoryEntry
import com.keyler.webvoyager.utils.HistoryManager

class HistoryActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_history)
		title = "History"

		val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_history)
		val textViewNoHistory = findViewById<TextView>(R.id.textViewNoHistory)
		
		val historyList = HistoryManager.getHistory(this).toMutableList()
		
		if (historyList.isEmpty()) {
			textViewNoHistory.visibility = View.VISIBLE
			recyclerView.visibility = View.GONE
		} else {
			textViewNoHistory.visibility = View.GONE
			recyclerView.visibility = View.VISIBLE
			val adapter = HistoryAdapter(historyList, this)
			recyclerView.layoutManager = LinearLayoutManager(this)
			recyclerView.adapter = adapter
			
		
		val buttonClearAll = findViewById<Button>(R.id.buttonClearAll)
			buttonClearAll.setOnClickListener {
				AlertDialog.Builder(this)
				.setTitle("Delete all History")
				.setMessage("Do you want to delete all history?")
				.setPositiveButton("Yes") { _, _ ->
					HistoryManager.clearHistory(this)
					adapter.clearAll()
					textViewNoHistory.visibility = View.VISIBLE
					recyclerView.visibility = View.GONE
				}
				.setNegativeButton("Cancel", null)
				.show()
			}
		}
	}
}