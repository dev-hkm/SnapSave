package com.snapsave.app.ui.quicksave

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.snapsave.app.SnapSaveApp
import com.snapsave.app.core.CodeLanguage
import com.snapsave.app.core.FileStore
import com.snapsave.app.core.Names
import com.snapsave.app.core.QuickSaveTarget
import com.snapsave.app.core.SettingsRepository
import com.snapsave.app.core.StorageSettings
import com.snapsave.app.data.SnippetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuickSaveViewModel(
    private val repo: SnippetRepository,
    private val files: FileStore,
    private val settings: SettingsRepository
) : ViewModel() {

    val storageSettings: StateFlow<StorageSettings> = settings.storageSettings
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SettingsRepository.cachedStorageSettings ?: StorageSettings()
        )

    val customExtensions: StateFlow<Set<String>> = settings.customExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val recentExtensions: StateFlow<List<String>> = settings.recentExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var targetOverride by mutableStateOf<QuickSaveTarget?>(null)
        private set

    var savedMessage by mutableStateOf<String?>(null)
        private set
    var savedFileUri by mutableStateOf<Uri?>(null)
        private set
    var savedMimeType by mutableStateOf("text/plain")
        private set
    var saving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun updateTarget(target: QuickSaveTarget) {
        targetOverride = target
    }

    fun setCustomFolder(uri: String?, name: String?) {
        viewModelScope.launch { settings.setCustomFolder(uri, name) }
    }

    fun addCustomExtension(ext: String) {
        viewModelScope.launch { settings.addCustomExtension(ext) }
    }

    fun removeCustomExtension(ext: String) {
        viewModelScope.launch { settings.removeCustomExtension(ext) }
    }

    fun recordRecentExtension(ext: String) {
        viewModelScope.launch { settings.recordRecentExtension(ext) }
    }

    fun save(
        title: String,
        content: String,
        language: CodeLanguage,
        customExtension: String? = null
    ) {
        if (saving || savedMessage != null) return
        saving = true
        error = null

        viewModelScope.launch {
            val storage = settings.storageSettings.first()
            val theme = settings.themeSettings.first()
            val s = if (theme.language == "vi") com.snapsave.app.core.StringsVi else com.snapsave.app.core.StringsEn
            val effectiveTarget = targetOverride ?: storage.quickSaveTarget

            val ext = customExtension?.trim()?.removePrefix(".")?.lowercase()?.ifBlank { null }
                ?: language.extension
            val langLabel = if (!customExtension.isNullOrBlank()) {
                customExtension.trim().removePrefix(".").uppercase()
            } else {
                language.label
            }

            runCatching {
                val cleanTitle = title.trim().ifBlank {
                    Names.fileBase(title.ifBlank { "snippet_${System.currentTimeMillis() / 1000}" })
                }
                val fileName = "${Names.fileBase(cleanTitle)}.$ext"
                val mime = files.mimeFor(ext)

                val (msg, uri) = when (effectiveTarget) {
                    QuickSaveTarget.APP_ONLY -> {
                        val entity = repo.create(cleanTitle, content, langLabel, ext)
                        s.savedToAppOnly(entity.fileName) to files.uriFor(entity.fileName)
                    }
                    QuickSaveTarget.DEVICE_FOLDER -> {
                        val treeUriStr = storage.customFolderUri
                        if (treeUriStr.isNullOrBlank()) {
                            error(s.noFolderSelectedError)
                        }
                        val result = files.saveToCustomFolder(Uri.parse(treeUriStr), fileName, content)
                        val folderPath = result.getOrThrow()
                        // Vẫn lưu vào repo để theo dõi
                        val entity = repo.create(cleanTitle, content, langLabel, ext)
                        s.savedToDeviceOnly(folderPath) to files.uriFor(entity.fileName)
                    }
                    QuickSaveTarget.BOTH -> {
                        val treeUriStr = storage.customFolderUri
                        val entity = repo.create(cleanTitle, content, langLabel, ext)
                        val fileUri = files.uriFor(entity.fileName)
                        if (!treeUriStr.isNullOrBlank()) {
                            val result = files.saveToCustomFolder(Uri.parse(treeUriStr), fileName, content)
                            val folderPath = result.getOrThrow()
                            s.savedToAppAndFolder(folderPath) to fileUri
                        } else {
                            s.savedToAppOnly(entity.fileName) to fileUri
                        }
                    }
                }
                savedFileUri = uri
                savedMimeType = mime
                msg
            }.onSuccess { message ->
                savedMessage = message
                settings.recordRecentExtension(ext)
                if (!customExtension.isNullOrBlank()) {
                    settings.addCustomExtension(customExtension)
                }
            }.onFailure { ex ->
                error = ex.message ?: s.saveError
            }
            saving = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SnapSaveApp
                QuickSaveViewModel(
                    repo = app.container.repository,
                    files = app.container.fileStore,
                    settings = app.container.settings
                )
            }
        }
    }
}
