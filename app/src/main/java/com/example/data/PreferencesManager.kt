package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore delegation
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "miconfig_settings")

class PreferencesManager(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("milauncher_layout_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val layoutAdapter = moshi.adapter(HomeLayoutJson::class.java)

    companion object {
        // DataStore keys
        val GRID_SIZE_KEY = stringPreferencesKey("grid_size") // "4x5", "4x6", "5x5"
        val ICON_SIZE_KEY = stringPreferencesKey("icon_size") // "Small", "Medium", "Large"
        val SHOW_LABELS_KEY = booleanPreferencesKey("show_labels")
        val LABEL_COLOR_KEY = stringPreferencesKey("label_color") // "White", "Black", "Auto"
        val DOCK_BG_KEY = stringPreferencesKey("dock_bg") // "Transparent", "Frosted", "Solid"
        val TRANSITION_STYLE_KEY = stringPreferencesKey("transition_style") // "Slide", "Fade", "Scale"
    }

    // --- DataStore (Settings) Operations ---

    val gridSizeFlow: Flow<String> = context.dataStore.data.map { it[GRID_SIZE_KEY] ?: "4x5" }
    val iconSizeFlow: Flow<String> = context.dataStore.data.map { it[ICON_SIZE_KEY] ?: "Medium" }
    val showLabelsFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LABELS_KEY] ?: true }
    val labelColorFlow: Flow<String> = context.dataStore.data.map { it[LABEL_COLOR_KEY] ?: "White" }
    val dockBgFlow: Flow<String> = context.dataStore.data.map { it[DOCK_BG_KEY] ?: "Frosted" }
    val transitionStyleFlow: Flow<String> = context.dataStore.data.map { it[TRANSITION_STYLE_KEY] ?: "Slide" }

    suspend fun saveGridSize(value: String) {
        context.dataStore.edit { it[GRID_SIZE_KEY] = value }
    }

    suspend fun saveIconSize(value: String) {
        context.dataStore.edit { it[ICON_SIZE_KEY] = value }
    }

    suspend fun saveShowLabels(value: Boolean) {
        context.dataStore.edit { it[SHOW_LABELS_KEY] = value }
    }

    suspend fun saveLabelColor(value: String) {
        context.dataStore.edit { it[LABEL_COLOR_KEY] = value }
    }

    suspend fun saveDockBg(value: String) {
        context.dataStore.edit { it[DOCK_BG_KEY] = value }
    }

    suspend fun saveTransitionStyle(value: String) {
        context.dataStore.edit { it[TRANSITION_STYLE_KEY] = value }
    }

    // Since we sometimes need synchronous setup for ViewModel defaults to avoid flickering:
    fun getSyncGridSize(): String = "4x5"

    // --- SharedPreferences (Layout) Operations ---

    fun saveHomeLayout(layout: HomeLayoutJson) {
        try {
            val json = layoutAdapter.toJson(layout)
            sharedPrefs.edit().putString("saved_layout", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHomeLayout(): HomeLayoutJson? {
        val json = sharedPrefs.getString("saved_layout", null) ?: return null
        return try {
            layoutAdapter.fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearSavedLayout() {
        sharedPrefs.edit().remove("saved_layout").apply()
    }
}
