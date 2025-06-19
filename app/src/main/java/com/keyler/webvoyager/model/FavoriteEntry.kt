package com.keyler.webvoyager.model

data class FavoriteEntry(
    val title: String,
    val url: String,
    val dateAdded: Long = System.currentTimeMillis()
)
