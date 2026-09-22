package com.snapsave.app.service

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb

data class SnippetOverlayPalette(
    val surfaceColor: Int,
    val surfaceVariantColor: Int,
    val primaryColor: Int,
    val primaryContainerColor: Int,
    val onPrimaryContainerColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val outlineColor: Int,
    val outlineVariantColor: Int,
    val accentColor: Int,
    val selectedChipContainerColor: Int,
    val selectedChipContentColor: Int,
    val selectedChipStrokeColor: Int?,
    val isDark: Boolean
)

object SnippetOverlayPaletteResolver {

    fun isDark(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun resolve(context: Context): SnippetOverlayPalette {
        val isDark = isDark(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            SnippetOverlayPalette(
                surfaceColor = scheme.surface.toArgb(),
                surfaceVariantColor = scheme.surfaceVariant.toArgb(),
                primaryColor = scheme.primary.toArgb(),
                primaryContainerColor = scheme.primaryContainer.toArgb(),
                onPrimaryContainerColor = scheme.onPrimaryContainer.toArgb(),
                textColor = scheme.onSurface.toArgb(),
                mutedTextColor = scheme.onSurfaceVariant.toArgb(),
                outlineColor = scheme.outline.toArgb(),
                outlineVariantColor = scheme.outlineVariant.toArgb(),
                accentColor = scheme.tertiary.toArgb(),
                selectedChipContainerColor = scheme.primary.toArgb(),
                selectedChipContentColor = scheme.onPrimary.toArgb(),
                selectedChipStrokeColor = null,
                isDark = isDark
            )
        } else {
            if (isDark) {
                SnippetOverlayPalette(
                    surfaceColor = android.graphics.Color.parseColor("#131318"),
                    surfaceVariantColor = android.graphics.Color.parseColor("#262A34"),
                    primaryColor = android.graphics.Color.parseColor("#BBC3FF"),
                    primaryContainerColor = android.graphics.Color.parseColor("#2E3CC8"),
                    onPrimaryContainerColor = android.graphics.Color.parseColor("#DFE1FF"),
                    textColor = android.graphics.Color.parseColor("#F5F7FA"),
                    mutedTextColor = android.graphics.Color.parseColor("#9EA5B5"),
                    outlineColor = android.graphics.Color.parseColor("#444955"),
                    outlineVariantColor = android.graphics.Color.parseColor("#343A48"),
                    accentColor = android.graphics.Color.parseColor("#E6BAD8"),
                    selectedChipContainerColor = android.graphics.Color.parseColor("#BBC3FF"),
                    selectedChipContentColor = android.graphics.Color.parseColor("#12226B"),
                    selectedChipStrokeColor = null,
                    isDark = true
                )
            } else {
                SnippetOverlayPalette(
                    surfaceColor = android.graphics.Color.parseColor("#FBF8FF"),
                    surfaceVariantColor = android.graphics.Color.parseColor("#E1E4EE"),
                    primaryColor = android.graphics.Color.parseColor("#4654E0"),
                    primaryContainerColor = android.graphics.Color.parseColor("#DFE1FF"),
                    onPrimaryContainerColor = android.graphics.Color.parseColor("#00125C"),
                    textColor = android.graphics.Color.parseColor("#191C20"),
                    mutedTextColor = android.graphics.Color.parseColor("#44474F"),
                    outlineColor = android.graphics.Color.parseColor("#74777F"),
                    outlineVariantColor = android.graphics.Color.parseColor("#C4C6D0"),
                    accentColor = android.graphics.Color.parseColor("#77536D"),
                    selectedChipContainerColor = android.graphics.Color.parseColor("#4654E0"),
                    selectedChipContentColor = android.graphics.Color.WHITE,
                    selectedChipStrokeColor = null,
                    isDark = false
                )
            }
        }
    }
}
