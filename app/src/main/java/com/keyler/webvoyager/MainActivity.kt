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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebChromeClient
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
import com.keyler.webvoyager.model.FavoriteEntry

import com.keyler.webvoyager.model.HistoryEntry
import com.keyler.webvoyager.utils.BookmarkManager
import com.keyler.webvoyager.utils.HistoryManager
import com.keyler.webvoyager.utils.UpdateChecker

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
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        editTextUrl = findViewById(R.id.editTextUrl)
        webView = findViewById(R.id.webView)
        buttonGo = findViewById(R.id.buttonGo)
        buttonBack = findViewById(R.id.buttonBack)
        buttonForward = findViewById(R.id.buttonForward)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        progressBar = findViewById(R.id.progressBar)
		watermarkBottom = findViewById(R.id.watermark_bottom)
		buttonHome = findViewById(R.id.buttonHome)
		editTextUrl = findViewById(R.id.editTextUrl)
		
		// Solo en uso de etapa temprana
		
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
                        BookmarkManager.saveBookmark(this, FavoriteEntry(title, url))
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_show_bookmarks -> {
                        val intent = Intent(this, BookmarkActivity::class.java)
                        startActivity(intent)
                        true
                    }
					R.id.action_show_history -> {
                        val intent = Intent(this, HistoryActivity::class.java)
                        startActivity(intent)
                        true
                    }
					R.id.downloads -> {
						val intent = Intent(this, DownloadsActivity::class.java)
                        startActivity(intent)
						true
					}
					R.id.update -> {
						val intent = Intent(this, UpdateActivity::class.java)
						startActivity(intent)
						true
					}
                    else -> false
                }
            }
            popup.show()
        }
		
		UpdateChecker.checkForUpdate(this) { isAvailable, changelog, url ->
			if (isAvailable) {
				runOnUiThread {
					AlertDialog.Builder(this)
					.setTitle("New version available")
					.setMessage("Do you want to update?\n\nChanges:\n$changelog")
					.setPositiveButton("Update") { _, _ ->
					val intent = Intent(this, UpdateActivity::class.java)
						intent.putExtra("download_url", url)
						startActivity(intent)
					}
					.setNegativeButton("Later", null)
					.show()
				}
			}
		}
		
		webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            try {
                val safeMimeType = mimeType ?: "*/*"

                // Intentar obtener el nombre del archivo con respaldo seguro
                val rawFilename = try {
                    URLUtil.guessFileName(url, contentDisposition, safeMimeType)
                } catch (e: Exception) {
                    null
                }

                val filename = if (!rawFilename.isNullOrBlank()) {
                    if (!rawFilename.contains(".")) "$rawFilename.dat" else rawFilename
                } else {
                    "downloaded_file_${System.currentTimeMillis()}.dat"
                }

                AlertDialog.Builder(this)
                    .setTitle("Download")
                    .setMessage("Do you want to download \"$filename\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        startDownload(url, filename, safeMimeType)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error preparing download: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
		
		webView.webChromeClient = object : WebChromeClient() {
			override fun onProgressChanged(view: WebView?, newProgress: Int) {
				progressBarLocal.isIndeterminate = false
				progressBarLocal.max = 100
				progressBarLocal.progress = newProgress
		
				if (newProgress < 100) {
					progressBarLocal.visibility = View.VISIBLE
				} else {
					progressBarLocal.visibility = View.GONE
				}
			}
		}

        webClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
				progressBarLocal.visibility = View.VISIBLE
				if (!navigationHistory.contains(url) && url != null) {
                    navigationHistory.add(url)
                    if (navigationHistory.size > 10) {
                        navigationHistory.removeAt(0)
                    }
                }
				url?.let { editTextUrl.setText(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
				progressBar.visibility = View.GONE
				progressBar.isIndeterminate = true
                val title = view?.title ?: url ?: "Sin título"
                val validUrl = url ?: return
                HistoryManager.saveEntry(this@MainActivity, HistoryEntry(title, validUrl, System.currentTimeMillis()))
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
		
		buttonHome.setOnClickListener {
            webView.loadUrl(homePageUrl)
            editTextUrl.setText(homePageUrl)
        }
		
		editTextUrl.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                val url = editTextUrl.text.toString()
                if (url.isNotBlank()) {
                    webView.loadUrl(url)

                    // Ocultar el teclado
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editTextUrl.windowToken, 0)
                }
                true
            } else {
                false
            }
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
                val bookmark = FavoriteEntry(webView.title ?: "No title", webView.url ?: "")
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
	
	private fun startDownload(url: String, filename: String, mimeType: String) {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType)
            setTitle(filename)
            setDescription("Downloading file...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            allowScanningByMediaScanner()
        }

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
    }
}