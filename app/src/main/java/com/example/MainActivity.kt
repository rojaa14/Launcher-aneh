package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    enum class Screen {
        Splash,
        Home,
        Settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge immersive drawing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(Screen.Splash) }

                // ViewModel declarations
                val homeViewModel: HomeViewModel = viewModel()
                val settingsViewModel: SettingsViewModel = viewModel()

                // Intercept back actions for native desktop feeling (pressing back inside pages or settings returns home)
                val onBackPressedDispatcher = this.onBackPressedDispatcher
                val backCallback = remember {
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            when (currentScreen) {
                                Screen.Settings -> {
                                    currentScreen = Screen.Home
                                }
                                Screen.Home -> {
                                    if (homeViewModel.isDrawerOpen.value) {
                                        homeViewModel.toggleDrawer(false)
                                    } else if (homeViewModel.isOverviewMode.value) {
                                        homeViewModel.toggleOverviewMode(false)
                                    } else if (homeViewModel.activeFolder.value != null) {
                                        homeViewModel.closeFolder()
                                    } else if (homeViewModel.currentPageIndex.value != 0) {
                                        homeViewModel.setPageIndex(0)
                                    } else {
                                        // Minimize or ignore back presses on home page index 0 to match actual launch desktop
                                        moveTaskToBack(true)
                                    }
                                }
                                else -> {
                                    isEnabled = false
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                    }
                }

                // Register the back press interceptor
                androidx.compose.runtime.DisposableEffect(onBackPressedDispatcher) {
                    onBackPressedDispatcher.addCallback(backCallback)
                    onDispose {
                        backCallback.remove()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Outer structural controller
                    when (currentScreen) {
                        Screen.Splash -> {
                            SplashScreen(
                                onNavigateToHome = {
                                    currentScreen = Screen.Home
                                    promptLauncherSelection()
                                }
                            )
                        }
                        Screen.Home -> {
                            HomeScreen(
                                homeViewModel = homeViewModel,
                                settingsViewModel = settingsViewModel,
                                onNavigateToSettings = {
                                    currentScreen = Screen.Settings
                                }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBackToHome = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper to prompt the device default home selector intent on initial home start.
     */
    private fun promptLauncherSelection() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
