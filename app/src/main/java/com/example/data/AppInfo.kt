package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val iconResName: String? = null, // Preset key for beautiful custom graphics/icons (e.g. "phone", "chrome", "messages", "camera")
    val isMock: Boolean = false
)

@JsonClass(generateAdapter = true)
data class HomeItemJson(
    val isFolder: Boolean,
    val folderId: String? = null,
    val label: String,
    val app: AppInfo? = null,
    val folderApps: List<AppInfo> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PageJson(
    val items: List<HomeItemJson>
)

@JsonClass(generateAdapter = true)
data class HomeLayoutJson(
    val pages: List<PageJson>,
    val dock: List<AppInfo>
)
