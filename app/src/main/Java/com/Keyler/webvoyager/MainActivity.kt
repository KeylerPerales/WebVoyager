package com.keyler.webvoyager

import android.app.DownloadManager
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.keyler.webvoyager.model.Bookmark
import com.keyler.webvoyager.utils.BookmarkManager

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUrl: EditText
    private lateinit var webView: WebView
    private lateinit var buttonGo: ImageButton
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonForward: ImageButton
    private lateinit var buttonRefresh: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var webClient: WebViewClient
	private val navigationHistory = mutableListOf<String>()
    private lateinit var buttonHistory: ImageButton
	private lateinit var watermarkBottom: TextView
	private lateinit var buttonHome: ImageButton
	
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
		watermarkBottom = findViewById(R.id.watermark_bottom)
		buttonHome = findViewById(R.id.buttonHome)
		
		// Solo para informacion de etapa temprana
		
		/*val watermarkBottom = findViewById<TextView>(R.id.watermark_bottom) // Inicializar la variable

        val fullText = "WebVoyager Codename \"Alpha\"\nFor testing purpose only. Build 158"
        val boldText = "WebVoyager Codename \"Alpha\""
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(boldText)
        val endIndex = startIndex + boldText.length

        if (startIndex != -1) {
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        watermarkBottom.text = spannableString*/
		
        val progressBarLocal = findViewById<ProgressBar>(R.id.progressBar)
		
		val webSettings: WebSettings = webView.settings
		
		val homePageUrl = "https://www.google.com" // Puedes cambiar esta URL a la que prefiera
		
		webSettings.javaScriptEnabled = true
		
		val btnMenu = findViewById<ImageButton>(R.id.btn_popup_menu)
        btnMenu.setOnClickListener {
            val popup = PopupMenu(this, btnMenu)
            popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
			try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add_bookmark -> {
                        val url = webView.url ?: return@setOnMenuItemClickListener false
                        val title = webView.title ?: url
                        BookmarkManager.saveBookmark(this, Bookmark(title, url))
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_show_bookmarks -> {
                        val intent = Intent(this, BookmarkActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
		
		webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))

            request.setMimeType(mimetype)
            val filename = contentDisposition?.substringAfter("filename*=UTF-8''")?.substringBefore(";")
                ?: contentDisposition?.substringAfter("filename=\"")?.substringBefore("\"")
                ?: url.substringAfterLast("/")

            request.setDescription("Downloading file")
            request.setTitle(filename)

            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this@MainActivity, "Download started", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this@MainActivity, "Error to load page", Toast.LENGTH_LONG).show()
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
		
		buttonHome.setOnClickListener {
            webView.loadUrl(homePageUrl)
            editTextUrl.setText(homePageUrl)
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
            webView.loadUrl(homePageUrl) // Usa la misma URL de la variable homePageUrl
            navigationHistory.add(homePageUrl)
            editTextUrl.setText(homePageUrl) // Establecer la URL inicial en el EditText
        }
    }
	
	private fun showHistoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, navigationHistory)

        builder.setAdapter(adapter) { dialog, which ->
            val selectedUrl = navigationHistory[which]
            webView.loadUrl(selectedUrl)
        }

        builder.setNegativeButton("Close") { dialog, _ ->
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
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_bookmark -> {
                val bookmark = Bookmark(webView.title ?: "No title", webView.url ?: "")
                BookmarkManager.saveBookmark(this, bookmark)
                Toast.makeText(this, "Saved favorite", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_show_bookmarks -> {
                startActivity(Intent(this, BookmarkActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}