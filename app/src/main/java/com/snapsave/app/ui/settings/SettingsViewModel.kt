package com.snapsave.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.snapsave.app.SnapSaveApp
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.SettingsRepository
import com.snapsave.app.core.StorageSettings
import com.snapsave.app.core.ThemeSettings
import com.snapsave.app.data.SnippetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val repo: SnippetRepository
) : ViewModel() {

    val theme: StateFlow<ThemeSettings> = settings.themeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeSettings())

    val storage: StateFlow<StorageSettings> = settings.storageSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StorageSettings())

    /** (số lượng snippet, tổng dung lượng byte) */
    val stats: StateFlow<Pair<Int, Long>> = repo.snippets
        .map { list -> list.size to list.sumOf { it.sizeBytes } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0 to 0L)

    var themeModeOverride by androidx.compose.runtime.mutableStateOf<Int?>(null)
        private set

    var languageOverride by androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    fun setThemeMode(mode: Int) {
        themeModeOverride = mode
        viewModelScope.launch {
            settings.setThemeMode(mode)
            themeModeOverride = null
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { settings.setHaptics(enabled) }
    }

    fun setShowSearchBar(enabled: Boolean) {
        viewModelScope.launch { settings.setShowSearchBar(enabled) }
    }

    fun setShowCategoryBar(enabled: Boolean) {
        viewModelScope.launch { settings.setShowCategoryBar(enabled) }
    }

    fun setIsGridView(isGrid: Boolean) {
        viewModelScope.launch { settings.setIsGridView(isGrid) }
    }

    fun setLanguage(language: String) {
        languageOverride = language
        viewModelScope.launch {
            settings.setLanguage(language)
            languageOverride = null
        }
    }

    fun setQuickSaveTarget(target: QuickSaveTarget) {
        viewModelScope.launch { settings.setQuickSaveTarget(target) }
    }

    fun setCustomFolder(uri: String?, name: String?) {
        viewModelScope.launch { settings.setCustomFolder(uri, name) }
    }

    fun clearAll(onDone: () -> Unit) {
        viewModelScope.launch {
            val list = repo.snippets.first()
            runCatching { repo.deleteAll(list) }
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SnapSaveApp
                SettingsViewModel(app.container.settings, app.container.repository)
            }
        }
    }
}
