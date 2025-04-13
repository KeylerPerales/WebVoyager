package com.keyler.webvoyager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log // Importa la clase Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.appcompat.content.res.AppCompatResources
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var goButton: ImageButton
    private lateinit var webView: WebView
    private lateinit var backButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var reloadButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var favoritesButton: ImageButton
    private lateinit var addToFavoritesButton: ImageButton

    private val navigationHistory = ArrayList<String>()
    private var historyIndex = -1

    private val favorites = ArrayList<String>()
    private val sharedPreferences by lazy { getSharedPreferences("favorites", Context.MODE_PRIVATE) }
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener referencias a los elementos de la UI
        urlEditText = findViewById(R.id.urlEditText)
        goButton = findViewById(R.id.goButton)
        webView = findViewById(R.id.webView)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        reloadButton = findViewById(R.id.reloadButton)
        progressBar = findViewById(R.id.progressBar)
        favoritesButton = findViewById(R.id.favoritesButton)
        addToFavoritesButton = findViewById(R.id.addToFavoritesButton)

        loadFavorites() // Cargar los favoritos guardados al iniciar la actividad

        // Verificar si se abrió un favorito
        intent.getStringExtra("favorite_url")?.let { url ->
            urlEditText.setText(url)
            webView.loadUrl(url)
        }

        // Habilitar JavaScript
        webView.settings.javaScriptEnabled = true

        // Establecer un WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                progressBar.isIndeterminate = false
                backButton.isEnabled = historyIndex > 0
                forwardButton.isEnabled = historyIndex < navigationHistory.size - 1

                if (url != null && (historyIndex == navigationHistory.size - 1 || url != navigationHistory.getOrNull(historyIndex))) {
                    navigationHistory.add(url)
                    historyIndex = navigationHistory.size - 1
                }
            }
        }

        // Listeners de los botones
        goButton.setOnClickListener {
            val url = urlEditText.text.toString()
            if (url.isNotEmpty()) {
                webView.clearCache(true)
                webView.loadUrl(url)
                navigationHistory.clear()
                navigationHistory.add(url)
                historyIndex = 0
                updateNavigationButtons()
            }
        }

        backButton.setOnClickListener {
            if (historyIndex > 0) {
                historyIndex--
                webView.loadUrl(navigationHistory[historyIndex])
                updateNavigationButtons()
            }
        }

        forwardButton.setOnClickListener {
            if (historyIndex < navigationHistory.size - 1) {
                historyIndex++
                webView.loadUrl(navigationHistory[historyIndex])
                updateNavigationButtons()
            }
        }

        reloadButton.setOnClickListener {
            webView.reload()
        }

        addToFavoritesButton.setOnClickListener {
            val currentUrl = webView.url
            Log.d("Favorites", "Current URL: $currentUrl")
            if (!currentUrl.isNullOrEmpty() && !favorites.contains(currentUrl)) {
                Log.d("Favorites", "Adding URL to favorites: $currentUrl")
                favorites.add(currentUrl)
                saveFavorites()
                // Opcional: Puedes enviar un Broadcast o usar alguna otra forma de comunicar el cambio a FavoritesActivity si está activa

                // Log para verificar que se añadió a la lista en MainActivity
                Log.d("Favorites", "Favorites list in MainActivity after adding: $favorites")
            } else {
                Log.d("Favorites", "URL is null/empty or already in favorites.")
            }
        }

        favoritesButton.setOnClickListener {
            Log.d("Favorites", "Favorites button pressed.") // Log
            // Aquí abriremos una nueva actividad o diálogo para mostrar los favoritos
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()
                return@OnKeyListener true
            }
            false
        })

        updateNavigationButtons()
    }

    fun saveFavorites() {
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString("favorites_list", json).apply()
        Log.d("Favorites", "Favorites saved to SharedPreferences: $json")
    }

    fun loadFavorites() {
        val json = sharedPreferences.getString("favorites_list", null)
        Log.d("Favorites", "Loaded favorites JSON: $json")
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<ArrayList<String>>() {}.type
            val loadedFavorites = gson.fromJson<ArrayList<String>>(json, type)
            if (loadedFavorites != null) {
                favorites.clear()
                favorites.addAll(loadedFavorites)
                Log.d("Favorites", "Favorites loaded in MainActivity: $favorites")
            }
        }
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = historyIndex > 0
        forwardButton.isEnabled = historyIndex < navigationHistory.size - 1
    }
}