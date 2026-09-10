package com.snapsave.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snapsave.app.core.AppHaptics
import com.snapsave.app.core.ThemeSettings
import com.snapsave.app.ui.navigation.AppNavHost
import com.snapsave.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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

        val settings = (application as SnapSaveApp).container.settings

        setContent {
            val theme by settings.themeSettings.collectAsStateWithLifecycle(initialValue = ThemeSettings())
            val strings = if (theme.language == "vi") com.snapsave.app.core.StringsVi else com.snapsave.app.core.StringsEn

            LaunchedEffect(theme.hapticsEnabled) {
                AppHaptics.enabled = theme.hapticsEnabled
            }

            androidx.compose.runtime.CompositionLocalProvider(
                com.snapsave.app.core.LocalAppStrings provides strings
            ) {
                AppTheme(themeMode = theme.themeMode, dynamicColor = theme.dynamicColor) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
