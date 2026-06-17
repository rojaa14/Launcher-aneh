package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.data.HomeItemJson
import com.example.utils.IconUtils
import com.example.utils.SquircleShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    item: HomeItemJson,
    iconSize: Dp,
    showLabel: Boolean,
    labelTextColor: Color,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanTag = remember(item) {
        if (item.isFolder) "folder_item_${item.folderId}" else "app_item_${item.app?.packageName}"
    }

    Column(
        modifier = modifier
            .testTag(cleanTag)
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (item.isFolder) {
            // Folder shows a 2x2 preview of icons inside a squircle container
            FolderIconPreview(
                folderApps = item.folderApps,
                iconSize = iconSize
            )
        } else {
            // Standard individual app
            item.app?.let { app ->
                AppIconGraphic(
                    app = app,
                    iconSize = iconSize
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            
            // Drop shadow styling for readable text on colorful backgrounds
            Text(
                text = item.label,
                color = labelTextColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun AppIconGraphic(
    app: AppInfo,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Query native app drawable in safety block
    val realBitmap = remember(app) {
        if (!app.isMock) {
            val drawable = IconUtils.getAppIcon(context, app.packageName, app.className)
            drawable?.let { drawableToBitmap(it) }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .size(iconSize)
            .clip(SquircleShape())
            .shadow(
                elevation = 2.dp,
                shape = SquircleShape(),
                clip = false
            ),
        contentAlignment = Alignment.Center
    ) {
        if (realBitmap != null) {
            // Render actual system package icon
            androidx.compose.foundation.Image(
                bitmap = realBitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated gradient-drawn high-fidelity icon matching Xiaomi's design tokens
            SimulatedIconBackground(preset = app.iconResName ?: "")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderIconPreview(
    folderApps: List<AppInfo>,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    // Elegant frosted glass-like background for the folder icon
    Box(
        modifier = modifier
            .size(iconSize)
            .clip(SquircleShape())
            .background(Color.White.copy(alpha = 0.28f))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (folderApps.isEmpty()) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Empty Folder Grid",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxSize(0.6f)
            )
        } else {
            // Max 2x2 grid representing the contents inside
            val previewItems = folderApps.take(4)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RowOfIcons(rowApps = previewItems.take(2))
                if (previewItems.size > 2) {
                    RowOfIcons(rowApps = previewItems.drop(2))
                }
            }
        }
    }
}

@Composable
private fun RowOfIcons(rowApps: List<AppInfo>) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (app in rowApps) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(1.5.dp)
            ) {
                AppIconGraphic(app = app, iconSize = 20.dp, modifier = Modifier.align(Alignment.Center))
            }
        }
        if (rowApps.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SimulatedIconBackground(preset: String) {
    val gradient = getPresetGradient(preset)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient),
        contentAlignment = Alignment.Center
    ) {
        val iconVector = getPresetVector(preset)
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize(0.55f)
        )
    }
}

private fun getPresetGradient(preset: String): Brush {
    val colors = when (preset) {
        "phone" -> listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)) // Deep emerald
        "browser" -> listOf(Color(0xFF1565C0), Color(0xFF2196F3)) // Sea azure
        "messages" -> listOf(Color(0xFF00ACC1), Color(0xFF26C6DA)) // Cyan drop
        "camera" -> listOf(Color(0xFF37474F), Color(0xFF546E7A)) // Industrial slate
        "settings" -> listOf(Color(0xFF455A64), Color(0xFF78909C)) // Silver titanium
        "gallery" -> listOf(Color(0xFFE91E63), Color(0xFFFF4081)) // Vivid coral sunset
        "weather" -> listOf(Color(0xFF0288D1), Color(0xFF29B6F6)) // Celestial sky
        "notes" -> listOf(Color(0xFFF57C00), Color(0xFFFFB74D)) // Saffron amber
        "calculator" -> listOf(Color(0xFFE64A19), Color(0xFFFF5722)) // Bright hot orange
        "music" -> listOf(Color(0xFF8E24AA), Color(0xFFBA68C8)) // Premium royal purple
        "security" -> listOf(Color(0xFF1B5E20), Color(0xFF388E3C)) // Protective army green
        "files" -> listOf(Color(0xFFFBC02D), Color(0xFFFFF176)) // Bright gold yellow
        "youtube" -> listOf(Color(0xFFD32F2F), Color(0xFFFF1744)) // YouTube crimson red
        "playstore" -> listOf(Color(0xFF512DA8), Color(0xFF673AB7)) // Deep indigo card
        else -> listOf(Color(0xFF424242), Color(0xFF757575)) // Neutral template
    }
    return Brush.verticalGradient(colors)
}

private fun getPresetVector(preset: String) = when (preset) {
    "phone" -> Icons.Default.Phone
    "browser" -> Icons.Default.Language
    "messages" -> Icons.Default.Sms
    "camera" -> Icons.Default.CameraAlt
    "settings" -> Icons.Default.Settings
    "gallery" -> Icons.Default.PhotoLibrary
    "weather" -> Icons.Default.Cloud
    "notes" -> Icons.Default.StickyNote2
    "calculator" -> Icons.Default.Calculate
    "music" -> Icons.Default.MusicNote
    "security" -> Icons.Default.Security
    "files" -> Icons.Default.Folder
    "youtube" -> Icons.Default.PlayArrow
    "playstore" -> Icons.Default.CheckCircle
    else -> Icons.Default.QuestionMark
}

// Convert native Drawable to Bitmap helper for headless environment
private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
