package com.snapsave.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.snapsave.app.SnapSaveApp
import com.snapsave.app.core.CodeLanguage
import com.snapsave.app.core.SettingsRepository
import com.snapsave.app.data.SnippetEntity
import com.snapsave.app.data.SnippetRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.net.Uri
import com.snapsave.app.core.FileStore

data class HomeUiState(
    val all: List<SnippetEntity> = emptyList(),
    val visible: List<SnippetEntity> = emptyList(),
    val languages: List<String> = emptyList(),
    val query: String = "",
    val activeLanguage: String? = null,
    val tipDismissed: Boolean = true,
    val loading: Boolean = true,
    val showSearchBar: Boolean = true,
    val showCategoryBar: Boolean = true,
    val isGridView: Boolean = false
)

sealed interface HomeEvent {
    data object Deleted : HomeEvent
}

class HomeViewModel(
    private val repo: SnippetRepository,
    private val settings: SettingsRepository,
    private val files: FileStore
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val language = MutableStateFlow<String?>(null)

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HomeEvent> = _events

    val uiState: StateFlow<HomeUiState> = combine(
        repo.snippets, query, language, settings.themeSettings
    ) { list, q, lang, pref ->
        val filtered = list.filter { e ->
            (lang == null || e.language == lang) &&
                (
                    q.isBlank() ||
                        e.title.contains(q, ignoreCase = true) ||
                        e.preview.contains(q, ignoreCase = true) ||
                        e.language.contains(q, ignoreCase = true)
                    )
        }
        HomeUiState(
            all = list,
            visible = filtered,
            languages = list.map { it.language }.distinct().sorted(),
            query = q,
            activeLanguage = lang,
            tipDismissed = pref.tipDismissed,
            loading = false,
            showSearchBar = pref.showSearchBar,
            showCategoryBar = pref.showCategoryBar,
            isGridView = pref.isGridView
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setLanguage(value: String?) {
        language.value = value
    }

    // ---- Xóa + Hoàn tác ----
    private var lastDeleted: Pair<SnippetEntity, String>? = null

    fun delete(entity: SnippetEntity) {
        viewModelScope.launch {
            val content = runCatching { repo.content(entity) }.getOrDefault("")
            repo.delete(entity)
            lastDeleted = entity to content
            _events.emit(HomeEvent.Deleted)
        }
    }

    fun undoDelete() {
        val pending = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching { repo.restore(pending.first, pending.second) }
        }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            settings.setIsGridView(!uiState.value.isGridView)
        }
    }

    fun shareData(e: SnippetEntity): Pair<Uri, String> =
        files.uriFor(e.fileName) to files.mimeFor(e.extension)

    suspend fun getContent(e: SnippetEntity): String =
        runCatching { repo.content(e) }.getOrDefault("")

    fun dismissTip() {
        viewModelScope.launch { settings.setTipDismissed(true) }
    }

    fun create(title: String, content: String, language: CodeLanguage, onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { repo.create(title, content, language) }
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SnapSaveApp
                HomeViewModel(app.container.repository, app.container.settings, app.container.fileStore)
            }
        }
    }
}
