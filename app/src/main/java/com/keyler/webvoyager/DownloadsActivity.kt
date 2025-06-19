package com.keyler.webvoyager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.io.File

class DownloadsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var downloadFiles: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Downloads"
        setContentView(R.layout.activity_downloads)

        recyclerView = findViewById(R.id.recyclerViewDownloads)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadFiles = downloadDir.listFiles()?.toList() ?: emptyList()

        if (downloadFiles.isEmpty()) {
            Toast.makeText(this, "No downloads found", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = DownloadAdapter(downloadFiles)
    }

    inner class DownloadAdapter(private val files: List<File>) :
        RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

        inner class DownloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val fileName: TextView = view.findViewById(R.id.textFileName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_download, parent, false)
            return DownloadViewHolder(view)
        }

        override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
            val file = files[position]
            holder.fileName.text = file.name
            holder.itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@DownloadsActivity, "No app to open this file", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount(): Int = files.size
    }
}