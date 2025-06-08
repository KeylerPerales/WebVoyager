package com.keyler.webvoyager

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.keyler.webvoyager.utils.UpdateChecker
import java.io.File

class UpdateActivity : AppCompatActivity() {

	private lateinit var progressBar: ProgressBar
	private lateinit var textView: TextView
	private var downloadId: Long = -1
	private var downloadUrl: String? = null

	private lateinit var downloadManager: DownloadManager
	private val handler = Handler(Looper.getMainLooper())

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_update)
		title = "Updates"

		progressBar = findViewById(R.id.updateProgressBar)
		textView = findViewById(R.id.updateText)
		val startButton = findViewById<Button>(R.id.startUpdateButton)
		val checkButton = findViewById<Button>(R.id.checkUpdateButton)

		downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
		
		startButton.visibility = View.GONE

		// Buscar actualización
		checkButton.setOnClickListener {
			progressBar.isIndeterminate = true
			textView.text = "Searching updates..."
			UpdateChecker.checkForUpdate(this) { isAvailable, changelog, url ->
				runOnUiThread {
					progressBar.isIndeterminate = false
					if (isAvailable) {
						textView.text = "¡Actualización disponible!\n\n$changelog"
						downloadUrl = url
						startButton.visibility = View.VISIBLE
						Toast.makeText(this, "Actualización encontrada", Toast.LENGTH_SHORT).show()
					} else {
						textView.text = "No hay actualizaciones disponibles"
						startButton.visibility = View.GONE
					}
				}
			}
		}

		// Iniciar descarga
		startButton.setOnClickListener {
			if (downloadUrl != null) {
				downloadApk(downloadUrl!!)
			} else {
				Toast.makeText(this, "Primero busca una actualización", Toast.LENGTH_SHORT).show()
			}
		}
		registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
	}

	private fun downloadApk(url: String) {
		val request = DownloadManager.Request(Uri.parse(url))
		.setTitle("WebVoyager.apk")
		.setDescription("Downloading update...")
		.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "WebVoyager.apk")
		.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

		downloadId = downloadManager.enqueue(request)

		Toast.makeText(this, "Descarga iniciada...", Toast.LENGTH_SHORT).show()
		simulateProgress()
	}

	private fun simulateProgress() {
		progressBar.progress = 0
		progressBar.isIndeterminate = false

		Thread {
			while (true) {
				val query = DownloadManager.Query().setFilterById(downloadId)
				val cursor = downloadManager.query(query)
				if (cursor != null && cursor.moveToFirst()) {
					val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
					val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
					if (total > 0) {
						val progress = ((downloaded * 100) / total).toInt()
						handler.post {
							progressBar.progress = progress
							textView.text = "Downloading... $progress%"
						}
						if (downloaded >= total) {
							cursor.close()
							break
						}
					}
					cursor.close()
				}
				Thread.sleep(500)
			}
		}.start()
	}

	private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
			if (id == downloadId) {
				Toast.makeText(this@UpdateActivity, "Descarga completada", Toast.LENGTH_SHORT).show()

				val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "WebVoyager.apk")
				val apkUri = Uri.fromFile(apkFile)

				val installIntent = Intent(Intent.ACTION_VIEW)
				installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
				installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

				startActivity(installIntent)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		unregisterReceiver(onDownloadComplete)
	}
}