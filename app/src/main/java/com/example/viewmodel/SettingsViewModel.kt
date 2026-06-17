package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    val gridSize: StateFlow<String> = prefsManager.gridSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "4x5")

    val iconSize: StateFlow<String> = prefsManager.iconSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Medium")

    val showLabels: StateFlow<Boolean> = prefsManager.showLabelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val labelColor: StateFlow<String> = prefsManager.labelColorFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "White")

    val dockBg: StateFlow<String> = prefsManager.dockBgFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Frosted")

    val transitionStyle: StateFlow<String> = prefsManager.transitionStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Slide")

    fun setGridSize(value: String) {
        viewModelScope.launch { prefsManager.saveGridSize(value) }
    }

    fun setIconSize(value: String) {
        viewModelScope.launch { prefsManager.saveIconSize(value) }
    }

    fun setShowLabels(value: Boolean) {
        viewModelScope.launch { prefsManager.saveShowLabels(value) }
    }

    fun setLabelColor(value: String) {
        viewModelScope.launch { prefsManager.saveLabelColor(value) }
    }

    fun setDockBg(value: String) {
        viewModelScope.launch { prefsManager.saveDockBg(value) }
    }

    fun setTransitionStyle(value: String) {
        viewModelScope.launch { prefsManager.saveTransitionStyle(value) }
    }

    fun resetLayout(onResetComplete: () -> Unit) {
        viewModelScope.launch {
            prefsManager.clearSavedLayout()
            // Reset standard configurations as well
            prefsManager.saveGridSize("4x5")
            prefsManager.saveIconSize("Medium")
            prefsManager.saveShowLabels(true)
            prefsManager.saveLabelColor("White")
            prefsManager.saveDockBg("Frosted")
            prefsManager.saveTransitionStyle("Slide")
            onResetComplete()
        }
    }
}
