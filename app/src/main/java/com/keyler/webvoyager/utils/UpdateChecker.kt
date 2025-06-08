package com.keyler.webvoyager.utils

import android.content.Context
import android.util.Log

import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object UpdateChecker {
	fun checkForUpdate(
		context: Context,
		onResult: (isUpdateAvailable: Boolean, changelog: String?, downloadUrl: String?) -> Unit
	) {
		Thread {
			try {
				val url = URL("https://raw.githubusercontent.com/KeylerPerales/WebVoyager/main/version.json")
				val connection = url.openConnection() as HttpURLConnection
				connection.requestMethod = "GET"

				val reader = BufferedReader(InputStreamReader(connection.inputStream))
				val response = reader.readText()
				reader.close()

				val json = JSONObject(response)
				val latestVersion = json.getString("latest_version")
				val changelog = json.getString("changelog")
				val downloadUrl = json.getString("download_url")

				val currentVersion = context.packageManager
				.getPackageInfo(context.packageName, 0).versionName

				val isUpdateAvailable = latestVersion != currentVersion

				onResult(isUpdateAvailable, changelog, downloadUrl)
			} catch (e: Exception) {
				Log.e("UpdateChecker", "Error checking update", e)
				onResult(false, null, null)
			}
		}.start()
	}
}