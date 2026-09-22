package com.snapsave.app.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "snapsave_settings")

enum class QuickSaveTarget(val code: Int, val title: String) {
    APP_ONLY(0, "Lưu vào ứng dụng"),
    DEVICE_FOLDER(1, "Lưu vào thư mục máy"),
    BOTH(2, "Lưu cả hai (App & Máy)");

    companion object {
        fun fromCode(code: Int): QuickSaveTarget = entries.firstOrNull { it.code == code } ?: APP_ONLY
    }
}

data class ThemeSettings(
    val themeMode: Int = 0,   // 0 = system, 1 = light, 2 = dark
    val dynamicColor: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val tipDismissed: Boolean = false,
    val language: String = "en", // "en" (default) or "vi"
    val showSearchBar: Boolean = true,
    val showCategoryBar: Boolean = true,
    val isGridView: Boolean = false,
    val sortOrder: Int = 0, // 0 = Newest, 1 = Oldest, 2 = Title, 3 = Size
    val pinnedIds: Set<String> = emptySet()
)

data class StorageSettings(
    val quickSaveTarget: QuickSaveTarget = QuickSaveTarget.APP_ONLY,
    val customFolderUri: String? = null,
    val customFolderName: String? = null
)

class SettingsRepository(private val context: Context) {

    companion object {
        @Volatile
        var cachedStorageSettings: StorageSettings? = null
        @Volatile
        var cachedThemeSettings: ThemeSettings? = null
    }

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val TIP_DISMISSED = booleanPreferencesKey("tip_dismissed")
        val QUICK_SAVE_TARGET = intPreferencesKey("quick_save_target")
        val CUSTOM_FOLDER_URI = stringPreferencesKey("custom_folder_uri")
        val CUSTOM_FOLDER_NAME = stringPreferencesKey("custom_folder_name")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val SHOW_SEARCH_BAR = booleanPreferencesKey("show_search_bar")
        val SHOW_CATEGORY_BAR = booleanPreferencesKey("show_category_bar")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val SORT_ORDER = intPreferencesKey("sort_order")
        val PINNED_IDS = stringSetPreferencesKey("pinned_ids")
        val CUSTOM_EXTENSIONS = stringSetPreferencesKey("custom_extensions")
        val RECENT_EXTENSIONS = stringPreferencesKey("recent_extensions_csv")
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                themeSettings.first()
                storageSettings.first()
            }
        }
    }

    val themeSettings: Flow<ThemeSettings> = context.dataStore.data.map { p ->
        ThemeSettings(
            themeMode = p[Keys.THEME_MODE] ?: 0,
            dynamicColor = p[Keys.DYNAMIC_COLOR] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            tipDismissed = p[Keys.TIP_DISMISSED] ?: false,
            language = p[Keys.APP_LANGUAGE] ?: "en",
            showSearchBar = p[Keys.SHOW_SEARCH_BAR] ?: true,
            showCategoryBar = p[Keys.SHOW_CATEGORY_BAR] ?: true,
            isGridView = p[Keys.IS_GRID_VIEW] ?: false,
            sortOrder = p[Keys.SORT_ORDER] ?: 0,
            pinnedIds = p[Keys.PINNED_IDS] ?: emptySet()
        ).also { cachedThemeSettings = it }
    }

    val storageSettings: Flow<StorageSettings> = context.dataStore.data.map { p ->
        StorageSettings(
            quickSaveTarget = QuickSaveTarget.fromCode(p[Keys.QUICK_SAVE_TARGET] ?: 0),
            customFolderUri = p[Keys.CUSTOM_FOLDER_URI],
            customFolderName = p[Keys.CUSTOM_FOLDER_NAME]
        ).also { cachedStorageSettings = it }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.coerceIn(0, 2) }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    }

    suspend fun setTipDismissed(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.TIP_DISMISSED] = dismissed }
    }

    suspend fun setShowSearchBar(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_SEARCH_BAR] = enabled }
    }

    suspend fun setShowCategoryBar(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_CATEGORY_BAR] = enabled }
    }

    suspend fun setIsGridView(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.IS_GRID_VIEW] = isGrid }
    }

    suspend fun setSortOrder(order: Int) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.coerceIn(0, 3) }
    }

    suspend fun togglePinSnippet(snippetId: Long) {
        val idStr = snippetId.toString()
        context.dataStore.edit { p ->
            val current = p[Keys.PINNED_IDS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(idStr)) current.remove(idStr) else current.add(idStr)
            p[Keys.PINNED_IDS] = current
        }
    }

    suspend fun setQuickSaveTarget(target: QuickSaveTarget) {
        context.dataStore.edit { it[Keys.QUICK_SAVE_TARGET] = target.code }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = language }
    }

    suspend fun setCustomFolder(uri: String?, name: String?) {
        context.dataStore.edit {
            if (uri == null) it.remove(Keys.CUSTOM_FOLDER_URI) else it[Keys.CUSTOM_FOLDER_URI] = uri
            if (name == null) it.remove(Keys.CUSTOM_FOLDER_NAME) else it[Keys.CUSTOM_FOLDER_NAME] = name
        }
    }

    val customExtensions: Flow<Set<String>> = context.dataStore.data.map { p ->
        p[Keys.CUSTOM_EXTENSIONS] ?: emptySet()
    }

    val recentExtensions: Flow<List<String>> = context.dataStore.data.map { p ->
        p[Keys.RECENT_EXTENSIONS]
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun addCustomExtension(ext: String) {
        val clean = ext.trim().removePrefix(".").lowercase()
        if (clean.isBlank()) return
        context.dataStore.edit { p ->
            val current = p[Keys.CUSTOM_EXTENSIONS]?.toMutableSet() ?: mutableSetOf()
            current.add(clean)
            p[Keys.CUSTOM_EXTENSIONS] = current

            val recents = p[Keys.RECENT_EXTENSIONS]
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val updated = (listOf(clean) + recents.filter { it != clean }).take(20).joinToString(",")
            p[Keys.RECENT_EXTENSIONS] = updated
        }
    }

    suspend fun recordRecentExtension(ext: String) {
        val clean = ext.trim().removePrefix(".").lowercase()
        if (clean.isBlank()) return
        context.dataStore.edit { p ->
            val recents = p[Keys.RECENT_EXTENSIONS]
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val updated = (listOf(clean) + recents.filter { it != clean }).take(20).joinToString(",")
            p[Keys.RECENT_EXTENSIONS] = updated
        }
    }

    suspend fun removeCustomExtension(ext: String) {
        val clean = ext.trim().removePrefix(".").lowercase()
        context.dataStore.edit { p ->
            val current = p[Keys.CUSTOM_EXTENSIONS]?.toMutableSet() ?: mutableSetOf()
            current.remove(clean)
            p[Keys.CUSTOM_EXTENSIONS] = current

            val recents = p[Keys.RECENT_EXTENSIONS]
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() && it != clean }
                ?: emptyList()
            p[Keys.RECENT_EXTENSIONS] = recents.joinToString(",")
        }
    }
}
