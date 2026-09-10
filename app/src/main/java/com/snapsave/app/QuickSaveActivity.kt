package com.snapsave.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

/** Activity trong suốt: nhận text từ popup bôi đen (PROCESS_TEXT) hoặc Share (SEND), rồi hiện sheet Lưu nhanh. */
class QuickSaveActivity : ComponentActivity() {

    companion object {
        private const val MAX_CHARS = 2_000_000
    }

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

        val text = extractText(intent)
        if (text.isNullOrBlank()) {
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
                        QuickSaveSheet(initialText = text, onFinished = { finish() })
                    }
                }
            }
        }
    }

    private fun extractText(intent: Intent?): String? {
        if (intent == null) return null
        val raw: CharSequence? = when (intent.action) {
            // Đọc trực tiếp selection — KHÔNG đi qua clipboard nên không bị cắt văn bản dài.
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            Intent.ACTION_SEND -> intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            else -> null
        }
        val text = raw?.toString()?.take(MAX_CHARS)
        return text?.takeIf { it.isNotBlank() }
    }
}
