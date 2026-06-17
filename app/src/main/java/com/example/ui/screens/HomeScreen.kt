package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.data.HomeItemJson
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.SettingsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Subscribe to shared ViewModels values
    val layoutState by homeViewModel.layout.collectAsState()
    val isDrawerOpen by homeViewModel.isDrawerOpen.collectAsState()
    val isOverviewMode by homeViewModel.isOverviewMode.collectAsState()
    
    val gridSize by settingsViewModel.gridSize.collectAsState()
    val iconSizeClass by settingsViewModel.iconSize.collectAsState()
    val showLabels by settingsViewModel.showLabels.collectAsState()
    val labelColorStyle by settingsViewModel.labelColor.collectAsState()
    val dockBgStyle by settingsViewModel.dockBg.collectAsState()
    val transitionStyle by settingsViewModel.transitionStyle.collectAsState()

    // Map label color option to standard RGB
    val labelTextColor = when (labelColorStyle) {
        "Black" -> Color.Black
        else -> Color.White // "White" or "Auto" (White is best default on colorful wallpapers)
    }

    // Map icon size to units
    val iconSizeDp: Dp = when (iconSizeClass) {
        "Small" -> 44.dp
        "Large" -> 60.dp
        else -> 52.dp // "Medium"
    }

    // Resolve column layout grid constraints
    val (cols, rows) = when (gridSize) {
        "4x6" -> 4 to 6
        "5x5" -> 5 to 5
        else -> 4 to 5 // default 4x5
    }

    // Modal popup triggers
    val activeContextMenu by homeViewModel.activeContextMenu.collectAsState()
    val activeFolder by homeViewModel.activeFolder.collectAsState()

    // Manage Page swipes
    val pagerState = rememberPagerState(pageCount = { layoutState.pages.size })
    val currentPageIndex by homeViewModel.currentPageIndex.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        homeViewModel.setPageIndex(pagerState.currentPage)
    }

    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex < pagerState.pageCount && currentPageIndex >= 0) {
            pagerState.scrollToPage(currentPageIndex)
        }
    }

    // Quick Folder Merge Selecting State
    var mergingSourceItem by remember { mutableStateOf<HomeItemJson?>(null) }
    var mergeSelectionDialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_container")
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap to toggle overview mode directly!
                        homeViewModel.toggleOverviewMode(!isOverviewMode)
                    }
                )
            }
    ) {
        // LAYER 1: WALLPAPER BACKGROUND IMAGE
        Image(
            painter = painterResource(id = homeViewModel.wallpaperDrawableResId),
            contentDescription = "HyperOS Launcher Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // LAYER 2: ROOT CONTENT SCREEN OVERLAY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // --- ACTIVE RECORDING pill BANNER (Xiaomi Inspired) ---
            RecordingIndicatorBanner(viewModel = homeViewModel)

            // --- WIDGET AREA (Clock, Calendar, Battery) ---
            if (!isOverviewMode) {
                WidgetClock(
                    modifier = Modifier
                        .clickable { homeViewModel.toggleOverviewMode(true) } // Click widgets to trigger pinch overview easily!
                )
            } else {
                // Overview active header banner
                OverviewHeaderBanner(
                    onExit = { homeViewModel.toggleOverviewMode(false) },
                    onAdd = { homeViewModel.addNewBlankPage() }
                )
            }

            // --- PAGE INDICATOR / COMPRESS PREVIEWS GRID ---
            if (isOverviewMode) {
                // Previews multi-screen grids
                PagesOverviewGrid(
                    pages = layoutState.pages,
                    selectedPage = currentPageIndex,
                    onSelect = { 
                        homeViewModel.setPageIndex(it)
                        homeViewModel.toggleOverviewMode(false)
                    },
                    onDelete = { homeViewModel.deletePage(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Horizontal pager containing icons
                Box(modifier = Modifier.weight(1f)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val page = layoutState.pages.getOrNull(pageIndex)
                        if (page != null) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(cols),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("home_grid_page_$pageIndex")
                            ) {
                                itemsIndexed(page.items) { index, item ->
                                    AppIconItem(
                                        item = item,
                                        iconSize = iconSizeDp,
                                        showLabel = showLabels,
                                        labelTextColor = labelTextColor,
                                        onItemClick = {
                                            if (item.isFolder) {
                                                homeViewModel.openFolder(item)
                                            } else {
                                                item.app?.let { homeViewModel.launchApp(context, it) }
                                            }
                                        },
                                        onItemLongClick = {
                                            homeViewModel.showContextMenu(item, pageIndex)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pager dot indicators (if multiple screens exist and not in zoom overview)
            if (!isOverviewMode && layoutState.pages.size > 1) {
                PagerDotIndicators(
                    size = layoutState.pages.size,
                    currentSelected = currentPageIndex,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // --- FIXED SEARCH PILL BAR & UTILITIES (Positioned above dock) ---
            if (!isOverviewMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tap opens app drawer Bottom Sheet
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .clickable { homeViewModel.toggleDrawer(true) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("search_bar_clickable"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search apps...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    // Floating entry into layout Settings Screen
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Launcher layout settings config",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- BOTTOM FIXED DOCK (4 App slots) ---
            DockBar(
                dockApps = layoutState.dock,
                iconSize = iconSizeDp,
                showLabels = showLabels,
                labelTextColor = labelTextColor,
                dockBgStyle = dockBgStyle,
                onItemClick = { homeViewModel.launchApp(context, it) },
                onItemLongClick = { app, index ->
                    homeViewModel.showContextMenu(
                        item = HomeItemJson(isFolder = false, label = app.label, app = app),
                        pageIndex = currentPageIndex,
                        isDock = true,
                        dockIndex = index
                    )
                }
            )
        }

        // LAYER 3: APP DRAWER SHEET SLIDE OVERLAY (Bottom Sheet styled)
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerSheet(
                viewModel = homeViewModel,
                iconSize = iconSizeDp,
                showLabels = showLabels,
                onAppClick = { app ->
                    homeViewModel.toggleDrawer(false)
                    homeViewModel.launchApp(context, app)
                },
                onAppLongClick = { app ->
                    homeViewModel.toggleDrawer(false)
                    homeViewModel.showContextMenu(
                        item = HomeItemJson(isFolder = false, label = app.label, app = app),
                        pageIndex = currentPageIndex
                    )
                },
                onCloseDrawer = { homeViewModel.toggleDrawer(false) }
            )
        }

        // LAYER 4: POPUP FOLDER SELECTION WINDOWS
        activeFolder?.let { folder ->
            FolderPopup(
                folder = folder,
                iconSize = iconSizeDp,
                showLabels = showLabels,
                labelTextColor = labelTextColor,
                onAppClick = { app ->
                    homeViewModel.closeFolder()
                    homeViewModel.launchApp(context, app)
                },
                onExtractApp = { app ->
                    homeViewModel.removeAppFromFolder(folder.folderId!!, app, currentPageIndex)
                    Toast.makeText(context, "Extracting ${app.label} from folder", Toast.LENGTH_SHORT).show()
                },
                onRenameFolder = { newName ->
                    homeViewModel.renameFolder(folder.folderId!!, newName)
                },
                onDismiss = { homeViewModel.closeFolder() }
            )
        }

        // LAYER 5: CONTEXT LONG-PRESS MENU OVERLAY MODAL
        activeContextMenu?.let { menuState ->
            ContextMenuPopup(
                item = menuState.item,
                onOpen = {
                    if (menuState.item.isFolder) {
                        homeViewModel.openFolder(menuState.item)
                    } else {
                        menuState.item.app?.let { homeViewModel.launchApp(context, it) }
                    }
                },
                onAddToDock = {
                    menuState.item.app?.let { app ->
                        homeViewModel.addAppToDock(app, 0)
                        Toast.makeText(context, "${app.label} pinned to dock", Toast.LENGTH_SHORT).show()
                    }
                },
                onAppInfo = {
                    menuState.item.app?.let { homeViewModel.openAppInfo(context, it) }
                },
                onRemoveFromHome = {
                    homeViewModel.removeAppFromHomeScreen(menuState.item, menuState.pageIndex)
                    Toast.makeText(context, "${menuState.item.label} hidden from desktop", Toast.LENGTH_SHORT).show()
                },
                onMergeToFolder = {
                    mergingSourceItem = menuState.item
                    mergeSelectionDialogOpen = true
                },
                onDismiss = { homeViewModel.dismissContextMenu() }
            )
        }

        // LAYER 6: COMBINE SELECTOR DIALOG
        if (mergeSelectionDialogOpen && mergingSourceItem != null) {
            val currentPageItems = layoutState.pages.getOrNull(currentPageIndex)?.items ?: emptyList()
            val eligibleTargets = currentPageItems.filter { 
                it.folderId != mergingSourceItem!!.folderId && it.app?.packageName != mergingSourceItem!!.app?.packageName 
            }

            AlertDialog(
                onDismissRequest = { mergeSelectionDialogOpen = false },
                title = { Text("Select item to group/merge folder", color = Color.White) },
                containerColor = Color(0xFF1E1E28),
                textContentColor = Color.White,
                text = {
                    Column {
                        Text("Choose an app onto which you want to group \"${mergingSourceItem!!.label}\":", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(eligibleTargets) { _, target ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            homeViewModel.createOrAddToFolder(mergingSourceItem!!, target, currentPageIndex)
                                            mergeSelectionDialogOpen = false
                                            Toast.makeText(context, "Group completed!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = target.label,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { mergeSelectionDialogOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5400))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun RecordingIndicatorBanner(viewModel: HomeViewModel) {
    val isRecordingActive by viewModel.isRecordingActive.collectAsState()
    val durationSec by viewModel.recordingDurationSec.collectAsState()
    val isPaused by viewModel.isRecordingPaused.collectAsState()

    AnimatedVisibility(
        visible = isRecordingActive,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val seconds = durationSec % 60
        val minutes = durationSec / 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        // Waveform bounce progress animation
        val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
        val bounceOffset1 by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "line1"
        )
        val bounceOffset2 by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "line2"
        )
        val bounceOffset3 by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "line3"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE53935)) // Xiaomi Red active recording capsule
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("recording_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wave animated logo and timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Audio waveform visualization bounds
                    Row(
                        modifier = Modifier
                            .width(18.dp)
                            .height(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.weight(1f)) {
                            val activeStateHeight = size.height * (if (isPaused) 0.3f else bounceOffset1)
                            drawRect(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, (size.height - activeStateHeight) / 2),
                                size = androidx.compose.ui.geometry.Size(size.width, activeStateHeight)
                            )
                        }
                        Canvas(modifier = Modifier.weight(1f)) {
                            val activeStateHeight = size.height * (if (isPaused) 0.3f else bounceOffset2)
                            drawRect(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, (size.height - activeStateHeight) / 2),
                                size = androidx.compose.ui.geometry.Size(size.width, activeStateHeight)
                            )
                        }
                        Canvas(modifier = Modifier.weight(1f)) {
                            val activeStateHeight = size.height * (if (isPaused) 0.3f else bounceOffset3)
                            drawRect(
                                color = Color.White,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, (size.height - activeStateHeight) / 2),
                                size = androidx.compose.ui.geometry.Size(size.width, activeStateHeight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Screen Recording  $formattedTime",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interaction pill buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.pauseRecording() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Toggle Pause Screenrec",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { viewModel.stopRecording() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Screenrec",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewHeaderBanner(
    onExit: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Pinch Overview Desktop", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Rearrange, add or delete launcher grid pages", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }

        Row {
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5400))
            ) {
                Icon(Icons.Default.Add, contentDescription = "New blank sheet screen", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Overview Mode", tint = Color.White)
            }
        }
    }
}

@Composable
fun PagesOverviewGrid(
    pages: List<PageJson>,
    selectedPage: Int,
    onSelect: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(pages) { index, page ->
            val isSelected = index == selectedPage
            val outlineColor = if (isSelected) Color(0xFFFF5400) else Color.White.copy(alpha = 0.15f)
            val shadowWeight = if (isSelected) 4.dp else 1.dp

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x99242435)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .border(2.dp, outlineColor, RoundedCornerShape(14.dp))
                    .clickable { onSelect(index) },
                elevation = CardDefaults.cardElevation(defaultElevation = shadowWeight)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small preview count indicating apps inside
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFF5400) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Page Screen ${index + 1}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${page.items.size} Grid elements",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    // Delete button (only if there are multiple screens)
                    if (pages.size > 1) {
                        IconButton(
                            onClick = { onDelete(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Page Grid",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PagerDotIndicators(
    size: Int,
    currentSelected: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until size) {
            val isSelected = index == currentSelected
            val dotColor = if (isSelected) Color(0xFFFF5400) else Color.White.copy(alpha = 0.35f)
            val dotWidth = if (isSelected) 16.dp else 6.dp
            val dotHeight = 6.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .size(dotWidth, dotHeight)
            )
        }
    }
}
