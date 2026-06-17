package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppInfo
import com.example.data.HomeItemJson

@Composable
fun DockBar(
    dockApps: List<AppInfo>,
    iconSize: Dp,
    showLabels: Boolean,
    labelTextColor: Color,
    dockBgStyle: String, // "Transparent", "Frosted", "Solid"
    onItemClick: (AppInfo) -> Unit,
    onItemLongClick: (AppInfo, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Choose suitable frosted container coloring
    val containerBg = when (dockBgStyle) {
        "Transparent" -> Color.Transparent
        "Solid" -> Color(0xFF1E1E1E).copy(alpha = 0.95f)
        else -> Color.White.copy(alpha = 0.22f) // "Frosted" glass style
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dock_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dock line separator (subtle divider)
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 2.dp),
            thickness = 0.5.dp,
            color = Color.White.copy(alpha = 0.35f)
        )

        // Rounded pill dock container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(containerBg)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keep exactly 4 app slots
                val displayApps = dockApps.take(4)
                
                for (index in 0 until 4) {
                    val app = displayApps.getOrNull(index)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app != null) {
                            AppIconItem(
                                item = HomeItemJson(isFolder = false, label = app.label, app = app),
                                iconSize = iconSize,
                                showLabel = showLabels,
                                labelTextColor = labelTextColor,
                                onItemClick = { onItemClick(app) },
                                onItemLongClick = { onItemLongClick(app, index) }
                            )
                        } else {
                            // Blank dock slot placeholder
                            Box(
                                modifier = Modifier.size(iconSize),
                                contentAlignment = Alignment.Center
                            ) {
                                // Transparent spacing
                            }
                        }
                    }
                }
            }
        }
    }
}
