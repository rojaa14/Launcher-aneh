package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Subscribe to DataStore states
    val gridSize by viewModel.gridSize.collectAsState()
    val iconSize by viewModel.iconSize.collectAsState()
    val showLabels by viewModel.showLabels.collectAsState()
    val labelColor by viewModel.labelColor.collectAsState()
    val dockBg by viewModel.dockBg.collectAsState()
    val transitionStyle by viewModel.transitionStyle.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "MiLauncher Options Settings", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return home", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14141A)
                )
            )
        },
        containerColor = Color(0xFF0F0F14) // HyperOS Dark Onyx background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: SYSTEM WORKSPACE GRID
            item {
                SettingsHeader(title = "WORKSPACE GRID & ICONS")
                SettingsCard {
                    SettingsGroupSelector(
                        icon = Icons.Default.GridOn,
                        title = "Desktop Grid Size Layout",
                        options = listOf("4x5", "4x6", "5x5"),
                        currentSelected = gridSize,
                        onOptionSelected = { viewModel.setGridSize(it) }
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    SettingsGroupSelector(
                        icon = Icons.Default.PhotoSizeSelectLarge,
                        title = "App Icons Scale Spec",
                        options = listOf("Small", "Medium", "Large"),
                        currentSelected = iconSize,
                        onOptionSelected = { viewModel.setIconSize(it) }
                    )
                }
            }

            // SECTION 2: LABELS STYLE DISPLAY
            item {
                SettingsHeader(title = "WORKSPACE LABELS")
                SettingsCard {
                    SettingsSwitchItem(
                        icon = Icons.Default.Visibility,
                        title = "Show App Description Labels",
                        isChecked = showLabels,
                        onCheckChange = { viewModel.setShowLabels(it) }
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    SettingsGroupSelector(
                        icon = Icons.Default.ColorLens,
                        title = "Label Text Dynamic Color",
                        options = listOf("White", "Black", "Auto"),
                        currentSelected = labelColor,
                        onOptionSelected = { viewModel.setLabelColor(it) }
                    )
                }
            }

            // SECTION 3: BOTTOM DOCK & ANIMATIONS
            item {
                SettingsHeader(title = "DOCK & PAGE DECORATIONS")
                SettingsCard {
                    SettingsGroupSelector(
                        icon = Icons.Default.Layers,
                        title = "Dock Blur Background Style",
                        options = listOf("Transparent", "Frosted", "Solid"),
                        currentSelected = dockBg,
                        onOptionSelected = { viewModel.setDockBg(it) }
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    SettingsGroupSelector(
                        icon = Icons.Default.SwapHoriz,
                        title = "Page Swipe Transition Style",
                        options = listOf("Slide", "Fade", "Scale"),
                        currentSelected = transitionStyle,
                        onOptionSelected = { viewModel.setTransitionStyle(it) }
                    )
                }
            }

            // SECTION 4: ACTIONS AND SYSTEM RESET
            item {
                SettingsHeader(title = "LAUNCHER RECOVERY")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.resetLayout {
                                    Toast.makeText(context, "Launcher desktop restored successfully!", Toast.LENGTH_SHORT).show()
                                    onBackToHome()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Layout",
                            tint = Color(0xFFFF5400),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Reset Home Layout Grid",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Restores all folder structures and dock icons to defaults.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // SECTION 5: ABOUT LAUNCHER DEVELOPMENT
            item {
                SettingsHeader(title = "ABOUT DEVELOPER INFO")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About details",
                            tint = Color.Cyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "MiLauncher (HyperOS Core Overlay)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version: 1.0.0 (Offline-First Pro Edition)\nDeveloped under AI Studio Jetpack guidelines.\nXiaomi inspired superellipse squircle launcher.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Layout bottom margin spacing
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFFF5400), // Xiaomi signature orange orange
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF14141E)
        ),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF5400),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF2C2C35)
            ),
            modifier = Modifier.testTag("switch_$title")
        )
    }
}

@Composable
fun SettingsGroupSelector(
    icon: ImageVector,
    title: String,
    options: List<String>,
    currentSelected: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Beautiful horizontal block buttons selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (opt in options) {
                val isSelected = opt == currentSelected
                val btnBg = if (isSelected) Color(0xFFFF5400) else Color(0xFF242430)
                val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(btnBg)
                        .clickable { onOptionSelected(opt) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Sub scoping
typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope
