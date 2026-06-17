package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppInfo
import com.example.data.HomeItemJson
import com.example.data.HomeLayoutJson
import com.example.data.PageJson
import com.example.data.PreferencesManager
import com.example.utils.AppLoader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    // --- Core Launcher UI States ---
    private val _layout = MutableStateFlow<HomeLayoutJson>(HomeLayoutJson(emptyList(), emptyList()))
    val layout: StateFlow<HomeLayoutJson> = _layout.asStateFlow()

    private val _allAppsList = MutableStateFlow<List<AppInfo>>(emptyList())
    val allAppsList: StateFlow<List<AppInfo>> = _allAppsList.asStateFlow()

    private val _drawerSearchQuery = MutableStateFlow("")
    val drawerSearchQuery: StateFlow<String> = _drawerSearchQuery.asStateFlow()

    private val _recentApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val recentApps: StateFlow<List<AppInfo>> = _recentApps.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isOverviewMode = MutableStateFlow(false)
    val isOverviewMode: StateFlow<Boolean> = _isOverviewMode.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    // --- App Context Actions State ---
    private val _activeContextMenu = MutableStateFlow<ContextMenuState?>(null)
    val activeContextMenu: StateFlow<ContextMenuState?> = _activeContextMenu.asStateFlow()

    // --- Active Folder Interaction ---
    private val _activeFolder = MutableStateFlow<HomeItemJson?>(null)
    val activeFolder: StateFlow<HomeItemJson?> = _activeFolder.asStateFlow()

    // --- Simulating Premium Wallpaper Overlay or Custom Color themes ---
    val wallpaperDrawableResId = com.example.R.drawable.img_wallpaper

    // --- Hidden / Excluded Packages ---
    private val _hiddenPackages = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

    // --- Top Active Recording Pill State (Mimics Xiaomi HyperOS Banner) ---
    private val _isRecordingActive = MutableStateFlow(true) // simulated active initially for beautiful visual demo
    val isRecordingActive: StateFlow<Boolean> = _isRecordingActive.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(6) // 00:06 initial preview as requested
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private val _isRecordingPaused = MutableStateFlow(false)
    val isRecordingPaused: StateFlow<Boolean> = _isRecordingPaused.asStateFlow()

    private var recordingJob: Job? = null

    data class ContextMenuState(
        val item: HomeItemJson,
        val pageIndex: Int,
        val screenX: Float = 0f,
        val screenY: Float = 0f,
        val isDockItem: Boolean = false,
        val dockIndex: Int = -1
    )

    init {
        loadLauncherData()
        startRecordingTicker()
    }

    fun loadLauncherData() {
        viewModelScope.launch {
            // Read installed + mockup integrations
            val apps = AppLoader.loadAllApps(getApplication())
            _allAppsList.value = apps

            // Load saved layout, or fallback to default
            val saved = prefsManager.getHomeLayout()
            if (saved != null && saved.pages.isNotEmpty()) {
                _layout.value = saved
            } else {
                val itemsPerPage = 20 // Default 4x5 layout
                val defaultLayout = AppLoader.createDefaultLayout(apps, itemsPerPage)
                _layout.value = defaultLayout
                prefsManager.saveHomeLayout(defaultLayout)
            }

            // Init default recent apps
            _recentApps.value = apps.take(5)
        }
    }

    // --- Tickers & Simulation Animations ---
    private fun startRecordingTicker() {
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isRecordingActive.value && !_isRecordingPaused.value) {
                    _recordingDurationSec.value += 1
                }
            }
        }
    }

    fun pauseRecording() {
        _isRecordingPaused.value = !_isRecordingPaused.value
    }

    fun stopRecording() {
        _isRecordingActive.value = false
        recordingJob?.cancel()
    }

    fun restartRecording() {
        _recordingDurationSec.value = 0
        _isRecordingActive.value = true
        _isRecordingPaused.value = false
        startRecordingTicker()
    }

    // --- Context Menu Triggers ---
    fun showContextMenu(item: HomeItemJson, pageIndex: Int, isDock: Boolean = false, dockIndex: Int = -1) {
        _activeContextMenu.value = ContextMenuState(item, pageIndex, isDockItem = isDock, dockIndex = dockIndex)
    }

    fun dismissContextMenu() {
        _activeContextMenu.value = null
    }

    // --- Folder Popups ---
    fun openFolder(folder: HomeItemJson) {
        _activeFolder.value = folder
    }

    fun closeFolder() {
        _activeFolder.value = null
    }

    fun renameFolder(folderId: String, newName: String) {
        val currentLayout = _layout.value
        val updatedPages = currentLayout.pages.map { page ->
            PageJson(
                page.items.map { item ->
                    if (item.isFolder && item.folderId == folderId) {
                        item.copy(label = newName)
                    } else {
                        item
                    }
                }
            )
        }
        val layoutCopy = currentLayout.copy(pages = updatedPages)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)

        // Sync active popup
        _activeFolder.value?.let { active ->
            if (active.folderId == folderId) {
                _activeFolder.value = active.copy(label = newName)
            }
        }
    }

    // --- Drag and Drop / Layout Mutation Operators ---

    /**
     * Merges two items together on a home page to form a Folder,
     * or adds an item to an existing folder.
     */
    fun createOrAddToFolder(sourceItem: HomeItemJson, targetItem: HomeItemJson, pageIndex: Int) {
        val currentLayout = _layout.value
        if (pageIndex < 0 || pageIndex >= currentLayout.pages.size) return

        val pageItems = currentLayout.pages[pageIndex].items.toMutableList()
        val sourceIndex = pageItems.indexOfFirst { it.folderId == sourceItem.folderId && it.app == sourceItem.app }
        if (sourceIndex == -1) return

        pageItems.removeAt(sourceIndex)

        val targetIndex = pageItems.indexOfFirst { it.folderId == targetItem.folderId && it.app == targetItem.app }
        if (targetIndex == -1) return

        val target = pageItems[targetIndex]
        if (target.isFolder) {
            // Target is an existing folder: add source apps to inner list
            val sourceApps = if (sourceItem.isFolder) sourceItem.folderApps else listOfNotNull(sourceItem.app)
            val updatedFolder = target.copy(folderApps = target.folderApps + sourceApps)
            pageItems[targetIndex] = updatedFolder
        } else {
            // Target is a single app: merge both into a brand new folder
            val sourceApps = if (sourceItem.isFolder) sourceItem.folderApps else listOfNotNull(sourceItem.app)
            val targetApps = listOfNotNull(target.app)
            val newFolder = HomeItemJson(
                isFolder = true,
                folderId = UUID.randomUUID().toString(),
                label = "Folder",
                folderApps = targetApps + sourceApps
            )
            pageItems[targetIndex] = newFolder
        }

        val updatedPages = currentLayout.pages.toMutableList()
        updatedPages[pageIndex] = PageJson(pageItems)

        val layoutCopy = currentLayout.copy(pages = updatedPages)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)
    }

    /**
     * Removes an app from a folder popup and places it on the current home page grid.
     */
    fun removeAppFromFolder(folderId: String, app: AppInfo, pageIndex: Int) {
        val currentLayout = _layout.value
        if (pageIndex < 0 || pageIndex >= currentLayout.pages.size) return

        val pageItems = currentLayout.pages[pageIndex].items.toMutableList()
        val folderIndex = pageItems.indexOfFirst { it.isFolder && it.folderId == folderId }
        if (folderIndex == -1) return

        val folder = pageItems[folderIndex]
        val updatedFolderApps = folder.folderApps.filterNot { it.packageName == app.packageName }

        if (updatedFolderApps.isEmpty()) {
            // If empty, remove the folder completely
            pageItems.removeAt(folderIndex)
        } else if (updatedFolderApps.size == 1) {
            // If only 1 app left, turn the folder back into a single app
            val lastApp = updatedFolderApps[0]
            pageItems[folderIndex] = HomeItemJson(isFolder = false, label = lastApp.label, app = lastApp)
        } else {
            // Update the folder structure
            val updatedFolder = folder.copy(folderApps = updatedFolderApps)
            pageItems[folderIndex] = updatedFolder
            if (_activeFolder.value?.folderId == folderId) {
                _activeFolder.value = updatedFolder
            }
        }

        // Add the removed app back to the primary page grid as individual item
        pageItems.add(HomeItemJson(isFolder = false, label = app.label, app = app))

        val updatedPages = currentLayout.pages.toMutableList()
        updatedPages[pageIndex] = PageJson(pageItems)

        val layoutCopy = currentLayout.copy(pages = updatedPages)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)
    }

    fun removeAppFromHomeScreen(item: HomeItemJson, pageIndex: Int) {
        val currentLayout = _layout.value
        if (pageIndex < 0 || pageIndex >= currentLayout.pages.size) return

        val updatedPageItems = currentLayout.pages[pageIndex].items.filterNot { 
            if (item.isFolder) it.folderId == item.folderId else it.app?.packageName == item.app?.packageName
        }

        val updatedPages = currentLayout.pages.toMutableList()
        updatedPages[pageIndex] = PageJson(updatedPageItems)

        val layoutCopy = currentLayout.copy(pages = updatedPages)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)
        
        // Hide/exclude list adding
        item.app?.let {
            _hiddenPackages.value = _hiddenPackages.value + it.packageName
        }
    }

    fun addAppToDock(app: AppInfo, targetIndex: Int) {
        val currentLayout = _layout.value
        val updatedDock = currentLayout.dock.toMutableList()
        if (targetIndex >= 0 && targetIndex < updatedDock.size) {
            updatedDock[targetIndex] = app
        } else if (updatedDock.size < 4) {
            updatedDock.add(app)
        }
        val layoutCopy = currentLayout.copy(dock = updatedDock)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)
    }

    /**
     * Swaps/rearranges item positions inside home pages
     */
    fun reorderItems(fromIndex: Int, toIndex: Int, pageIndex: Int) {
        val currentLayout = _layout.value
        if (pageIndex < 0 || pageIndex >= currentLayout.pages.size) return

        val pageItems = currentLayout.pages[pageIndex].items.toMutableList()
        if (fromIndex in pageItems.indices && toIndex in pageItems.indices) {
            val item = pageItems.removeAt(fromIndex)
            pageItems.add(toIndex, item)

            val updatedPages = currentLayout.pages.toMutableList()
            updatedPages[pageIndex] = PageJson(pageItems)

            val layoutCopy = currentLayout.copy(pages = updatedPages)
            _layout.value = layoutCopy
            prefsManager.saveHomeLayout(layoutCopy)
        }
    }

    // --- Overview Pager Pages Operations ---
    fun addNewBlankPage() {
        val currentLayout = _layout.value
        val updatedPages = currentLayout.pages.toMutableList()
        updatedPages.add(PageJson(emptyList()))
        
        val layoutCopy = currentLayout.copy(pages = updatedPages)
        _layout.value = layoutCopy
        prefsManager.saveHomeLayout(layoutCopy)
        
        _currentPageIndex.value = updatedPages.size - 1
    }

    fun deletePage(pageIndex: Int) {
        val currentLayout = _layout.value
        if (currentLayout.pages.size <= 1) return // Need at least 1 screen page
        
        val updatedPages = currentLayout.pages.toMutableList()
        if (pageIndex in updatedPages.indices) {
            updatedPages.removeAt(pageIndex)
            val layoutCopy = currentLayout.copy(pages = updatedPages)
            _layout.value = layoutCopy
            prefsManager.saveHomeLayout(layoutCopy)
            
            _currentPageIndex.value = (pageIndex - 1).coerceAtLeast(0)
        }
    }

    // --- Launch App Intents / Systems Animations ---
    fun launchApp(context: Context, app: AppInfo) {
        // Track recently used apps
        val updatedRecent = (listOf(app) + _recentApps.value).distinctBy { it.packageName }.take(5)
        _recentApps.value = updatedRecent

        if (app.isMock) {
            // Mimic application launching animation showing status banner/activity
            Toast.makeText(context, "Opening HyperOS ${app.label}...", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Opening ${app.label}...", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open: ${app.label}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openAppInfo(context: Context, app: AppInfo) {
        if (app.isMock) {
            Toast.makeText(context, "HyperOS Info for ${app.label} (Simulated App)", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to view settings for ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setPageIndex(index: Int) {
        _currentPageIndex.value = index
    }

    fun toggleDrawer(isOpen: Boolean) {
        _isDrawerOpen.value = isOpen
        if (!isOpen) _drawerSearchQuery.value = "" // clear search on close
    }

    fun updateDrawerSearch(query: String) {
        _drawerSearchQuery.value = query
    }

    fun toggleOverviewMode(isOverview: Boolean) {
        _isOverviewMode.value = isOverview
    }
}
