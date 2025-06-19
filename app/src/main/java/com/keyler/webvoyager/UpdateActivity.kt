package com.keyler.webvoyager

import android.content.Intent
import android.net.Uri
import android.view.View
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.keyler.webvoyager.utils.UpdateChecker
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*

class UpdateActivity : AppCompatActivity() {

	private lateinit var progressBar: ProgressBar
	private lateinit var textView: TextView
	private lateinit var checkButton: Button
	private lateinit var startButton: Button
	private var downloadUrl: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_update)
		title = "Updates"

		progressBar = findViewById(R.id.updateProgressBar)
		textView = findViewById(R.id.updateText)
		checkButton = findViewById(R.id.checkUpdateButton)
		startButton = findViewById(R.id.startUpdateButton)

		startButton.visibility = View.GONE

		checkButton.setOnClickListener {
			progressBar.isIndeterminate = true
			textView.text = "Searching updates..."

			UpdateChecker.checkForUpdate(this) { isAvailable, changelog, url ->
				runOnUiThread {
					progressBar.isIndeterminate = false
					if (isAvailable) {
						textView.text = "¡Update Available!\n\n$changelog"
						downloadUrl = url
						startButton.visibility = View.VISIBLE
						Toast.makeText(this, "Update found", Toast.LENGTH_SHORT).show()
					} else {
						textView.text = "No updates available"
						startButton.visibility = View.GONE
					}
				}
			}
		}

		startButton.setOnClickListener {
			downloadUrl?.let { url ->
				downloadApk(url)
			} ?: Toast.makeText(this, "No URL", Toast.LENGTH_SHORT).show()
		}
	}

	private fun downloadApk(url: String) {
		progressBar.isIndeterminate = false
		progressBar.progress = 0
		textView.text = "Downloading APK..."

		Thread {
			try {
				val client = OkHttpClient()
				val request = Request.Builder().url(url).build()
				val response = client.newCall(request).execute()

				if (!response.isSuccessful) throw IOException("Error: ${response.code}")

				val apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "WebVoyager.apk")
				val sink = apkFile.outputStream().buffered()
				val source = response.body?.byteStream()

				val buffer = ByteArray(4096)
				var bytesRead: Int
				var totalBytes = 0L
				val contentLength = response.body?.contentLength() ?: -1

				while (source?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
					sink.write(buffer, 0, bytesRead)
					totalBytes += bytesRead

					val progress = if (contentLength > 0) ((totalBytes * 100) / contentLength).toInt() else -1
					runOnUiThread {
						progressBar.progress = progress
						textView.text = "Descargando... $progress%"
					}
				}

				sink.flush()
				sink.close()
				source?.close()

				runOnUiThread {
					Toast.makeText(this, "Descarga completada", Toast.LENGTH_SHORT).show()
					installApk(apkFile)
				}

			} catch (e: Exception) {
				runOnUiThread {
					Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
					textView.text = "Error de descarga"
				}
			}
		}.start()
	}

	private fun installApk(file: File) {
		val apkUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
		val installIntent = Intent(Intent.ACTION_VIEW)
		installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
		installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
		startActivity(installIntent)
	}
}