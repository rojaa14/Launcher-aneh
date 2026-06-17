package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.AppInfo
import com.example.data.HomeItemJson
import com.example.data.HomeLayoutJson
import com.example.data.PageJson

object AppLoader {

    /**
     * Set of standard HyperOS-inspired mock apps to populate the home workspace
     * if the physical device/emulator has very few launcher apps, or to complement them.
     */
    val mockApps = listOf(
        AppInfo("com.milauncher.mock.phone", "PhoneActivity", "Phone", "phone", true),
        AppInfo("com.milauncher.mock.browser", "BrowserActivity", "Browser", "browser", true),
        AppInfo("com.milauncher.mock.messages", "MessagesActivity", "Messages", "messages", true),
        AppInfo("com.milauncher.mock.camera", "CameraActivity", "Camera", "camera", true),
        AppInfo("com.milauncher.mock.settings", "SettingsActivity", "Settings Launcher", "settings", true),
        AppInfo("com.milauncher.mock.gallery", "GalleryActivity", "Gallery", "gallery", true),
        AppInfo("com.milauncher.mock.weather", "WeatherActivity", "Weather", "weather", true),
        AppInfo("com.milauncher.mock.notes", "NotesActivity", "Notes", "notes", true),
        AppInfo("com.milauncher.mock.calculator", "CalculatorActivity", "Calculator", "calculator", true),
        AppInfo("com.milauncher.mock.music", "MusicActivity", "Music", "music", true),
        AppInfo("com.milauncher.mock.security", "SecurityActivity", "Security", "security", true),
        AppInfo("com.milauncher.mock.files", "FilesActivity", "File Manager", "files", true),
        AppInfo("com.milauncher.mock.youtube", "YouTubeActivity", "YouTube", "youtube", true),
        AppInfo("com.milauncher.mock.playstore", "PlayStoreActivity", "Play Store", "playstore", true)
    )

    /**
     * Loads physical system apps from PackageManager and merges with mock defaults
     */
    fun loadAllApps(context: Context): List<AppInfo> {
        val result = mutableListOf<AppInfo>()
        val pm = context.packageManager
        
        // Query official launcher intent activities
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        try {
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val ownPackage = context.packageName
            
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName
                // Skip own launcher icon in grid
                if (pkg == ownPackage) continue
                
                val className = ri.activityInfo.name
                val label = ri.loadLabel(pm).toString()
                
                // If it is already in our list (by pkg name matching, like a system app), skip adding twice
                val iconPreset = getPresetIconKey(pkg)
                result.add(AppInfo(pkg, className, label, iconPreset, false))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Merge Mock Apps that aren't already represented by package overlays
        for (mock in mockApps) {
            val isPackageMatching = result.any { 
                it.packageName.contains(mock.packageName.removePrefix("com.milauncher.mock.")) ||
                mock.packageName == it.packageName 
            }
            if (!isPackageMatching) {
                result.add(mock)
            }
        }

        // Return sorted apps so alphabetical layout is smooth, except dock defaults
        return result.distinctBy { it.packageName }.sortedBy { it.label }
    }

    private fun getPresetIconKey(pkg: String): String? {
        val p = pkg.lowercase()
        return when {
            p.contains("phone") || p.contains("dialer") -> "phone"
            p.contains("chrome") || p.contains("browser") || p.contains("internet") -> "browser"
            p.contains("message") || p.contains("sms") || p.contains("mms") -> "messages"
            p.contains("camera") -> "camera"
            p.contains("setting") -> "settings"
            p.contains("gallery") || p.contains("photo") -> "gallery"
            p.contains("weather") -> "weather"
            p.contains("note") -> "notes"
            p.contains("calculator") -> "calculator"
            p.contains("music") -> "music"
            p.contains("security") || p.contains("cleaner") -> "security"
            p.contains("file") || p.contains("explorer") -> "files"
            p.contains("youtube") -> "youtube"
            p.contains("vending") || p.contains("playstore") -> "playstore"
            else -> null
        }
    }

    /**
     * Builds a default layout when no settings are found.
     * Puts Phone, Browser, Messages, Camera in the bottom dock.
     * Spans the rest across horizontal pages.
     */
    fun createDefaultLayout(allApps: List<AppInfo>, itemsPerPage: Int = 20): HomeLayoutJson {
        // Dock consists of default core apps
        val dockList = mutableListOf<AppInfo>()
        val remainingApps = allApps.toMutableList()

        // 1. Phone
        val phoneApp = remainingApps.firstOrNull { it.iconResName == "phone" } ?: mockApps[0]
        dockList.add(phoneApp)
        remainingApps.remove(phoneApp)

        // 2. Browser
        val browserApp = remainingApps.firstOrNull { it.iconResName == "browser" } ?: mockApps[1]
        dockList.add(browserApp)
        remainingApps.remove(browserApp)

        // 3. Messages
        val smsApp = remainingApps.firstOrNull { it.iconResName == "messages" } ?: mockApps[2]
        dockList.add(smsApp)
        remainingApps.remove(smsApp)

        // 4. Camera
        val cameraApp = remainingApps.firstOrNull { it.iconResName == "camera" } ?: mockApps[3]
        dockList.add(cameraApp)
        remainingApps.remove(cameraApp)

        // Ensure exactly 4 apps in dock, fallback if anything went wrong
        while (dockList.size < 4 && remainingApps.isNotEmpty()) {
            dockList.add(remainingApps.removeAt(0))
        }

        // Group remaining into lists (pages) of size `itemsPerPage`
        val pages = mutableListOf<PageJson>()
        var currentPageItems = mutableListOf<HomeItemJson>()

        for (app in remainingApps) {
            if (currentPageItems.size >= itemsPerPage) {
                pages.add(PageJson(currentPageItems))
                currentPageItems = mutableListOf()
            }
            currentPageItems.add(HomeItemJson(isFolder = false, label = app.label, app = app))
        }
        
        if (currentPageItems.isNotEmpty() || pages.isEmpty()) {
            pages.add(PageJson(currentPageItems))
        }

        return HomeLayoutJson(pages, dockList)
    }
}
