package com.snapsave.app

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.snapsave.app.core.AppHaptics
import com.snapsave.app.core.ThemeSettings
import com.snapsave.app.ui.quicksave.QuickSaveSheet
import com.snapsave.app.ui.theme.AppTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Activity trong suốt: nhận text từ popup bôi đen (PROCESS_TEXT), menu Share (SEND text/file), hoặc Open With (VIEW). */
class QuickSaveActivity : ComponentActivity() {

    companion object {
        private const val MAX_CHARS = 2_000_000
    }

    data class SharedPayload(
        val text: String,
        val initialTitle: String? = null,
        val initialExtension: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val payload = extractPayload(intent)
        if (payload == null || payload.text.isBlank()) {
            finish()
            return
        }

        val settings = (application as SnapSaveApp).container.settings

        setContent {
            val theme by settings.themeSettings.collectAsStateWithLifecycle(initialValue = ThemeSettings())
            val strings = if (theme.language == "vi") com.snapsave.app.core.StringsVi else com.snapsave.app.core.StringsEn
            AppHaptics.enabled = theme.hapticsEnabled

            androidx.compose.runtime.CompositionLocalProvider(
                com.snapsave.app.core.LocalAppStrings provides strings
            ) {
                AppTheme(themeMode = theme.themeMode, dynamicColor = theme.dynamicColor) {
                    Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                        QuickSaveSheet(
                            initialText = payload.text,
                            initialTitle = payload.initialTitle,
                            initialExtension = payload.initialExtension,
                            onFinished = { finish() }
                        )
                    }
                }
            }
        }
    }

    private fun extractPayload(intent: Intent?): SharedPayload? {
        if (intent == null) return null

        // 1. Text trực tiếp từ PROCESS_TEXT hoặc SEND EXTRA_TEXT
        val rawText: CharSequence? = when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            Intent.ACTION_SEND -> intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            else -> null
        }
        if (!rawText.isNullOrBlank()) {
            return SharedPayload(text = rawText.toString().take(MAX_CHARS))
        }

        // 2. File Stream từ ACTION_SEND, ACTION_VIEW hoặc clipData
        val streamUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: intent.data ?: intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri

        if (streamUri != null) {
            return extractPayloadFromUri(streamUri)
        }

        return null
    }

    private fun extractPayloadFromUri(uri: Uri): SharedPayload? {
        return runCatching {
            var fileName: String? = null
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }
            }
            if (fileName.isNullOrBlank()) {
                fileName = uri.lastPathSegment
            }

            var initialTitle: String? = null
            var initialExtension: String? = null
            if (!fileName.isNullOrBlank()) {
                val cleanName = fileName!!.substringAfterLast('/')
                val dotIndex = cleanName.lastIndexOf('.')
                if (dotIndex > 0) {
                    initialTitle = cleanName.substring(0, dotIndex)
                    initialExtension = cleanName.substring(dotIndex + 1).lowercase()
                } else {
                    initialTitle = cleanName
                }
            }

            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (content.isBlank()) return null

            SharedPayload(
                text = content.take(MAX_CHARS),
                initialTitle = initialTitle,
                initialExtension = initialExtension
            )
        }.getOrNull()
    }
}
