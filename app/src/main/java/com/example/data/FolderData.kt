package com.example.data

data class FolderData(
    val id: String,
    val name: String,
    val apps: List<AppInfo> = emptyList()
)
