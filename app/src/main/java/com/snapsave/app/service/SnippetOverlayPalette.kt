package com.snapsave.app.service

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build

data class SnippetOverlayPalette(
    val surfaceColor: Int,
    val surfaceVariantColor: Int,
    val primaryColor: Int,
    val primaryContainerColor: Int,
    val onPrimaryContainerColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val outlineColor: Int,
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
        return if (isDark) {
            // Dark Mode Palette
            val primary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try { context.getColor(android.R.color.system_accent1_200) } catch (_: Exception) { Color.parseColor("#90CAF9") }
            } else Color.parseColor("#90CAF9")

            val surface = Color.parseColor("#181A20")
            val surfaceVariant = Color.parseColor("#262A34")
            val text = Color.parseColor("#F5F7FA")
            val mutedText = Color.parseColor("#9EA5B5")
            val outline = Color.parseColor("#3A4050")
            val primaryContainer = Color.parseColor("#1E3A5F")
            val onPrimaryContainer = Color.parseColor("#D0E4FF")

            SnippetOverlayPalette(
                surfaceColor = surface,
                surfaceVariantColor = surfaceVariant,
                primaryColor = primary,
                primaryContainerColor = primaryContainer,
                onPrimaryContainerColor = onPrimaryContainer,
                textColor = text,
                mutedTextColor = mutedText,
                outlineColor = outline,
                accentColor = primary,
                selectedChipContainerColor = primary,
                selectedChipContentColor = Color.parseColor("#0D1B2A"),
                selectedChipStrokeColor = null,
                isDark = true
            )
        } else {
            // Light Mode Palette
            val primary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try { context.getColor(android.R.color.system_accent1_600) } catch (_: Exception) { Color.parseColor("#1976D2") }
            } else Color.parseColor("#1976D2")

            val surface = Color.parseColor("#FFFFFF")
            val surfaceVariant = Color.parseColor("#F1F3F7")
            val text = Color.parseColor("#1C1E21")
            val mutedText = Color.parseColor("#5A6270")
            val outline = Color.parseColor("#D5DAE2")
            val primaryContainer = Color.parseColor("#E3F2FD")
            val onPrimaryContainer = Color.parseColor("#0D47A1")

            SnippetOverlayPalette(
                surfaceColor = surface,
                surfaceVariantColor = surfaceVariant,
                primaryColor = primary,
                primaryContainerColor = primaryContainer,
                onPrimaryContainerColor = onPrimaryContainer,
                textColor = text,
                mutedTextColor = mutedText,
                outlineColor = outline,
                accentColor = primary,
                selectedChipContainerColor = primary,
                selectedChipContentColor = Color.WHITE,
                selectedChipStrokeColor = null,
                isDark = false
            )
        }
    }
}
