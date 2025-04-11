package com.keyler.webvoyager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private val favoritesList = mutableListOf<String>() // Cambiado a mutableListOf
    private val sharedPreferences by lazy { getSharedPreferences("favorites", Context.MODE_PRIVATE) }
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)

        favoritesAdapter = FavoritesAdapter(favoritesList,
            { url ->
                val intent = Intent(this@FavoritesActivity, MainActivity::class.java)
                intent.putExtra("favorite_url", url)
                startActivity(intent)
            },
            { urlToDelete -> // Implementación del callback para eliminar
                removeFavorite(urlToDelete)
            }
        )
        favoritesRecyclerView.adapter = favoritesAdapter

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        // Recarga los favoritos cada vez que la actividad vuelve a primer plano
        loadFavorites()
    }

    private fun loadFavorites() {
        val json = sharedPreferences.getString("favorites_list", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            val loadedFavorites = gson.fromJson<ArrayList<String>>(json, type)
            if (loadedFavorites != null) {
                favoritesList.clear()
                favoritesList.addAll(loadedFavorites)
                // Notifica al adaptador de los cambios en los datos
                favoritesAdapter.notifyDataSetChanged()
            }
        } else {
            // Si no hay favoritos, asegúrate de que la lista esté vacía y notifica al adaptador
            favoritesList.clear()
            favoritesAdapter.notifyDataSetChanged()
        }
    }

    private fun removeFavorite(urlToDelete: String) {
        favoritesList.remove(urlToDelete)
        saveFavorites() // Llama a la función para guardar la lista actualizada
        favoritesAdapter.notifyDataSetChanged() // Actualiza la vista
    }

    private fun saveFavorites() {
        val json = gson.toJson(favoritesList)
        sharedPreferences.edit().putString("favorites_list", json).apply()
    }
}

class FavoritesAdapter(
    private val favorites: MutableList<String>, // Cambiado a MutableList
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit // Nueva función de callback para eliminar
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urlTextView: TextView = itemView.findViewById(R.id.favoriteUrlTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteFavoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteUrl = favorites[position]
        holder.urlTextView.text = favoriteUrl
        holder.itemView.setOnClickListener { onItemClick(favoriteUrl) }
        holder.deleteButton.setOnClickListener {
            onDeleteClick(favoriteUrl) // Llama a la función de callback con la URL a eliminar
        }
    }

    override fun getItemCount() = favorites.size
}