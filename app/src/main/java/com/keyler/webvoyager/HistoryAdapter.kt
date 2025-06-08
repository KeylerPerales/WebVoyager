package com.keyler.webvoyager

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.keyler.webvoyager.model.HistoryEntry
import com.keyler.webvoyager.utils.HistoryManager

class HistoryAdapter(
	private var entries: MutableList<HistoryEntry>,
	private val context: Context
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

		inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			val title: TextView = view.findViewById(R.id.historyTitle)
			val url: TextView = view.findViewById(R.id.historyUrl)
			val deleteButton: ImageButton = view.findViewById(R.id.buttonDelete)
		}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
		.inflate(R.layout.item_history, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val entry = entries[position]
		holder.title.text = entry.title
		holder.url.text = entry.url

		// Abrir URL al hacer clic
		holder.itemView.setOnClickListener {
			val intent = Intent(context, MainActivity::class.java)
			intent.putExtra("url_to_open", entry.url)
			context.startActivity(intent)
		}

		// Eliminar entrada específica
		holder.deleteButton.setOnClickListener {
			HistoryManager.deleteEntry(context, entry)
			entries.removeAt(holder.adapterPosition)
			notifyItemRemoved(holder.adapterPosition)
			notifyItemRangeChanged(holder.adapterPosition, entries.size)
		}
	}

	override fun getItemCount(): Int = entries.size

	fun clearAll() {
		entries.clear()
		notifyDataSetChanged()
	}
}