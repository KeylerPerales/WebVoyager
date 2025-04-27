package com.keyler.webvoyager

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.webkit.WebSettings
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import android.webkit.DownloadListener
import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.os.Environment

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUrl: EditText
    private lateinit var webView: WebView
    private lateinit var buttonGo: Button
    private lateinit var buttonBack: Button
    private lateinit var buttonForward: Button
    private lateinit var buttonRefresh: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var webClient: WebViewClient
	private val navigationHistory = mutableListOf<String>()
    private lateinit var buttonHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        editTextUrl = findViewById(R.id.editTextUrl)
        webView = findViewById(R.id.webView)
        buttonGo = findViewById(R.id.buttonGo)
        buttonBack = findViewById(R.id.buttonBack)
        buttonForward = findViewById(R.id.buttonForward)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        progressBar = findViewById(R.id.progressBar)
		buttonHistory = findViewById(R.id.buttonHistory)

        val progressBarLocal = findViewById<ProgressBar>(R.id.progressBar)
		
		val webSettings: WebSettings = webView.settings
		
		webSettings.javaScriptEnabled = true
		
		webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))

            request.setMimeType(mimetype)
            val filename = contentDisposition?.substringAfter("filename*=UTF-8''")?.substringBefore(";")
                ?: contentDisposition?.substringAfter("filename=\"")?.substringBefore("\"")
                ?: url.substringAfterLast("/")

            request.setDescription("Descargando archivo")
            request.setTitle(filename)

            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this@MainActivity, "Descarga iniciada", Toast.LENGTH_LONG).show()
        })

        webClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBarLocal.visibility = android.view.View.VISIBLE
				progressBarLocal.isIndeterminate = true
				if (!navigationHistory.contains(url) && url != null) {
                    navigationHistory.add(url)
                    if (navigationHistory.size > 10) {
                        navigationHistory.removeAt(0)
                    }
                }
				url?.let { editTextUrl.setText(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBarLocal.visibility = android.view.View.GONE
				url?.let { editTextUrl.setText(it) }
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                progressBarLocal.visibility = android.view.View.GONE // Ocultar la barra de progreso en caso de error
                // Mostrar un mensaje de error al usuario
                Toast.makeText(this@MainActivity, "Error al cargar la página.", Toast.LENGTH_LONG).show()
                // Puedes también cargar una página de error personalizada en el WebView si lo deseas
                webView.loadUrl("file:///android_asset/error.html")
            }
        }
        webView.webViewClient = webClient

        buttonGo.setOnClickListener {
            val url = editTextUrl.text.toString()
            webView.loadUrl(url)
        }
        buttonBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        buttonForward.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }

        buttonRefresh.setOnClickListener {
            webView.reload()
        }
		
		buttonHistory.setOnClickListener {
            showHistoryDialog()
		}

        // Restaurar el estado del WebView si hay un estado guardado
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
			val savedHistory = savedInstanceState.getStringArrayList("history")
            if (savedHistory != null) {
                navigationHistory.addAll(savedHistory)
			}
        } else {
            // Cargar una página inicial si no hay estado guardado
            webView.loadUrl("https://www.google.com") // Puedes cambiar esta URL por la que prefieras
			navigationHistory.add("https://www.google.com")
			editTextUrl.setText("https://www.google.com") // Establecer la URL inicial en el EditText
        }
    }
	
	private fun showHistoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Historial de Navegación")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, navigationHistory)

        builder.setAdapter(adapter) { dialog, which ->
            val selectedUrl = navigationHistory[which]
            webView.loadUrl(selectedUrl)
        }

        builder.setNegativeButton("Cerrar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Guardar el estado del WebView
        webView.saveState(outState)
    }
}