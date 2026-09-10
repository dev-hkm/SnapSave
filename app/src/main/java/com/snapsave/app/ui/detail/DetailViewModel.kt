package com.snapsave.app.ui.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.snapsave.app.SnapSaveApp
import com.snapsave.app.core.FileStore
import com.snapsave.app.data.SnippetEntity
import com.snapsave.app.data.SnippetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.snapsave.app.core.SettingsRepository
import com.snapsave.app.core.StringsEn
import com.snapsave.app.core.StringsVi
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val repo: SnippetRepository,
    private val files: FileStore,
    private val settings: SettingsRepository
) : ViewModel() {

    private val idFlow = MutableStateFlow(-1L)

    val entity: StateFlow<SnippetEntity?> = idFlow
        .flatMapLatest { id -> if (id <= 0) flowOf(null) else repo.observe(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isPinned: StateFlow<Boolean> = combine(idFlow, settings.themeSettings) { id, theme ->
        if (id <= 0) false else theme.pinnedIds.contains(id.toString())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _content = MutableStateFlow<String?>(null)
    val content: StateFlow<String?> = _content

    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<String> = _messages

    fun bind(id: Long) {
        if (idFlow.value == id) return
        idFlow.value = id
        viewModelScope.launch {
            val e = repo.get(id) ?: return@launch
            if (_content.value == null) {
                _content.value = runCatching { repo.content(e) }.getOrElse { "" }
            }
        }
    }

    fun saveEdits(title: String, newContent: String, onDone: () -> Unit) {
        val e = entity.value ?: return
        viewModelScope.launch {
            val theme = settings.themeSettings.first()
            val s = if (theme.language == "vi") StringsVi else StringsEn
            runCatching {
                if (title != e.title) repo.rename(e, title)
                if (newContent != _content.value) repo.updateContent(e, newContent)
                _content.value = newContent
            }.onSuccess {
                _messages.tryEmit(s.savedEditsMsg)
            }.onFailure {
                _messages.tryEmit(it.message ?: s.saveError)
            }
            onDone()
        }
    }

    fun shareData(e: SnippetEntity): Pair<Uri, String> =
        files.uriFor(e.fileName) to files.mimeFor(e.extension)

    fun export() {
        val e = entity.value ?: return
        viewModelScope.launch {
            val theme = settings.themeSettings.first()
            val s = if (theme.language == "vi") StringsVi else StringsEn
            val c = _content.value ?: runCatching { repo.content(e) }.getOrDefault("")
            files.exportToDownloads(e.fileName, c)
                .onSuccess { _messages.tryEmit(s.savedToPath(it.toString())) }
                .onFailure { _messages.tryEmit(it.message ?: s.saveError) }
        }
    }

    fun togglePin() {
        val id = idFlow.value
        if (id > 0) {
            viewModelScope.launch {
                settings.togglePinSnippet(id)
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val e = entity.value ?: return
        viewModelScope.launch {
            runCatching { repo.delete(e) }
            onDeleted()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SnapSaveApp
                DetailViewModel(app.container.repository, app.container.fileStore, app.container.settings)
            }
        }
    }
}
