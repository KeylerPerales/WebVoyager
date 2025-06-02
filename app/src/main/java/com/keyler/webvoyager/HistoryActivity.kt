package com.keyler.webvoyager

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.keyler.webvoyager.utils.HistoryManager

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val historyList = HistoryManager.getHistory(this)
        recyclerView.adapter = HistoryAdapter(historyList)
    }
}