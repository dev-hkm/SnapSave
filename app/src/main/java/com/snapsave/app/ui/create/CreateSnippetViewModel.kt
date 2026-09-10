package com.snapsave.app.ui.create

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
import com.snapsave.app.core.LanguageDetector
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

class CreateSnippetViewModel(
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

    var targetOverride by mutableStateOf<QuickSaveTarget?>(null)
        private set

    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var manualLanguage by mutableStateOf<CodeLanguage?>(null)
    var customExtension by mutableStateOf<String?>(null)

    val detectedLanguage: CodeLanguage
        get() = if (content.isNotBlank()) LanguageDetector.detect(content) else CodeLanguage.PLAIN

    val effectiveLanguage: CodeLanguage
        get() = manualLanguage ?: detectedLanguage

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

    fun saveSnippet(onSuccess: (Long) -> Unit) {
        if (saving || content.isBlank()) return
        saving = true
        error = null

        viewModelScope.launch {
            val storage = settings.storageSettings.first()
            val theme = settings.themeSettings.first()
            val s = if (theme.language == "vi") com.snapsave.app.core.StringsVi else com.snapsave.app.core.StringsEn
            val effectiveTarget = targetOverride ?: storage.quickSaveTarget
            val lang = effectiveLanguage
            val ext = customExtension?.trim()?.removePrefix(".")?.lowercase()?.ifBlank { null }
                ?: lang.extension
            val langLabel = if (!customExtension.isNullOrBlank()) {
                customExtension!!.trim().removePrefix(".").uppercase()
            } else {
                lang.label
            }

            runCatching {
                val cleanTitle = title.trim().ifBlank {
                    LanguageDetector.suggestTitle(content, lang)
                }
                val fileName = "${Names.fileBase(cleanTitle)}.$ext"

                val entity = repo.create(cleanTitle, content, langLabel, ext)

                if (effectiveTarget == QuickSaveTarget.DEVICE_FOLDER || effectiveTarget == QuickSaveTarget.BOTH) {
                    val treeUriStr = storage.customFolderUri
                    if (!treeUriStr.isNullOrBlank()) {
                        files.saveToCustomFolder(Uri.parse(treeUriStr), fileName, content).getOrThrow()
                    }
                }

                entity.id
            }.onSuccess { id ->
                saving = false
                onSuccess(id)
            }.onFailure { ex ->
                error = ex.message ?: s.saveError
                saving = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SnapSaveApp
                CreateSnippetViewModel(
                    repo = app.container.repository,
                    files = app.container.fileStore,
                    settings = app.container.settings
                )
            }
        }
    }
}
