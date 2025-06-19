package com.keyler.webvoyager

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.keyler.webvoyager.model.FavoriteEntry

class FavoritesAdapter(
    private var bookmarks: MutableList<FavoriteEntry>,
    private val context: Context
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.bookmarkTitle)
        val url: TextView = view.findViewById(R.id.bookmarkUrl)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonDeleteBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmark = bookmarks[position]
        holder.title.text = bookmark.title
        holder.url.text = bookmark.url

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("url_to_open", bookmark.url)
            context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            bookmarks.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            notifyItemRangeChanged(holder.adapterPosition, bookmarks.size)
        }
    }

    override fun getItemCount(): Int = bookmarks.size

    fun updateData(newList: List<FavoriteEntry>) {
        bookmarks.clear()
        bookmarks.addAll(newList)
        notifyDataSetChanged()
    }
}