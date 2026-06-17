package com.example.data

data class HomeLayoutData(
    val pages: List<List<HomeItemJson>> = emptyList(),
    val dock: List<AppInfo> = emptyList()
)
