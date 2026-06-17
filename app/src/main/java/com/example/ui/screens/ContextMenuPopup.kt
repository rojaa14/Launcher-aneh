package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HomeItemJson

@Composable
fun ContextMenuPopup(
    item: HomeItemJson,
    onOpen: () -> Unit,
    onAddToDock: () -> Unit,
    onAppInfo: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onMergeToFolder: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Backdrop blur dimming
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("context_menu_overlay")
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                onClick = onDismiss,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        // Centered modal floating near natural positions
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xEE2A2A38) // Dark Slate Premium HyperOS tone
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = modifier
                .align(Alignment.Center)
                .widthIn(max = 280.dp)
                .padding(16.dp)
                .testTag("context_menu_card")
                .clickable(enabled = false) {} // block clicks on card itself
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // Header Details
                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.7.dp)

                // Menu items
                ContextMenuItem(
                    icon = Icons.Default.Launch,
                    label = "Open App",
                    onClick = {
                        onOpen()
                        onDismiss()
                    }
                )

                if (!item.isFolder) {
                    ContextMenuItem(
                        icon = Icons.Default.MoveToInbox,
                        label = "Add to Bottom Dock",
                        onClick = {
                            onAddToDock()
                            onDismiss()
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.FolderOpen,
                        label = "Combine / Merge App",
                        onClick = {
                            onMergeToFolder()
                            onDismiss()
                        }
                    )
                }

                ContextMenuItem(
                    icon = Icons.Default.Info,
                    label = "Application Detail Info",
                    onClick = {
                        onAppInfo()
                        onDismiss()
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.7.dp)

                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    label = "Hide from Launcher",
                    iconColor = Color(0xFFFF5252),
                    labelColor = Color(0xFFFF5252),
                    onClick = {
                        onRemoveFromHome()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconColor: Color = Color.White.copy(alpha = 0.85f),
    labelColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
