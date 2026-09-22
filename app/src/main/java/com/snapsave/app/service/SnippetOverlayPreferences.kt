package com.snapsave.app.service

import android.content.Context

enum class OverlayAfterCopyAction(val id: String) {
    CLOSE_POPUP("close_popup"),
    KEEP_OPEN("keep_open");

    companion object {
        fun fromId(id: String?): OverlayAfterCopyAction =
            entries.find { it.id == id } ?: CLOSE_POPUP
    }
}

enum class OverlayStartFilterMode(val id: String, val title: String) {
    ALL("all", "All snippets"),
    PINNED("pinned", "Pinned"),
    FREQUENT("frequent", "Frequently used"),
    LAST_USED("last_used", "Last used filter"),
    CUSTOM_CATEGORY("custom", "Custom category");

    companion object {
        fun fromId(id: String?): OverlayStartFilterMode =
            entries.find { it.id == id } ?: ALL
    }
}

/**
 * Persisted local-only preferences for the floating quick-snippet overlay and popup.
 * Direct 1:1 parity with StickHub's OverlayPreferences.
 */
object SnippetOverlayPreferences {
    private const val PREFS_NAME = "snapsave_overlay_preferences"

    private const val KEY_BUBBLE_SIZE_DP = "bubble_size_dp"
    private const val KEY_SHOW_TITLE = "show_quick_snippets_title"
    private const val KEY_SHOW_SEARCH = "show_quick_snippets_search"
    private const val KEY_SHOW_CATEGORIES = "show_quick_snippets_categories"

    private const val KEY_BUBBLE_POS_FRACTION_X = "bubble_pos_fraction_x"
    private const val KEY_BUBBLE_POS_FRACTION_Y = "bubble_pos_fraction_y"

    private const val KEY_PANEL_WIDTH = "panel_width_px"
    private const val KEY_PANEL_HEIGHT = "panel_height_px"
    private const val KEY_PANEL_X = "panel_pos_x"
    private const val KEY_PANEL_Y = "panel_pos_y"

    // Multi-layer opacities
    private const val KEY_BUBBLE_OPACITY = "bubble_opacity"
    private const val KEY_POPUP_MASTER_OPACITY = "popup_master_opacity"
    private const val KEY_POPUP_SURFACE_OPACITY = "popup_surface_opacity"
    private const val KEY_POPUP_SNIPPETS_OPACITY = "popup_snippets_opacity"
    private const val KEY_POPUP_CHROME_OPACITY = "popup_chrome_opacity"
    private const val KEY_POPUP_CLOSE_OPACITY = "popup_close_opacity"
    private const val KEY_POPUP_RESIZE_OPACITY = "popup_resize_opacity"

    // Snippet shadow
    private const val KEY_SNIPPET_SHADOW_STRENGTH = "snippet_shadow_strength"

    private const val KEY_START_FILTER_MODE = "start_filter_mode"
    private const val KEY_START_CUSTOM_CATEGORY = "start_custom_category"
    private const val KEY_LAST_USED_FILTER = "last_used_filter"

    private const val KEY_AFTER_COPY_ACTION = "after_copy_action"

    const val DEFAULT_BUBBLE_SIZE_DP = 44f
    const val MIN_BUBBLE_SIZE_DP = 32f
    const val MAX_BUBBLE_SIZE_DP = 72f

    const val DEFAULT_BUBBLE_FRACTION_X = 0.94f
    const val DEFAULT_BUBBLE_FRACTION_Y = 0.35f

    // Bubble position
    fun bubblePositionFractionX(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_POS_FRACTION_X, DEFAULT_BUBBLE_FRACTION_X)
        .coerceIn(0f, 1f)

    fun bubblePositionFractionY(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_POS_FRACTION_Y, DEFAULT_BUBBLE_FRACTION_Y)
        .coerceIn(0f, 1f)

    fun setBubblePositionFraction(context: Context, fractionX: Float, fractionY: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_POS_FRACTION_X, fractionX.coerceIn(0f, 1f))
            .putFloat(KEY_BUBBLE_POS_FRACTION_Y, fractionY.coerceIn(0f, 1f))
            .apply()
    }

    // Bubble size
    fun bubbleSizeDp(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_SIZE_DP, DEFAULT_BUBBLE_SIZE_DP)
        .coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP)

    fun setBubbleSizeDp(context: Context, sizeDp: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_SIZE_DP, sizeDp.coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP))
            .apply()
    }

    // --- Multi-Layer Opacities ---

    fun bubbleOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_OPACITY, OverlayOpacityPolicy.DEFAULT_BUBBLE_OPACITY)
        .coerceIn(0f, 1f)

    fun setBubbleOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupMasterOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_MASTER_OPACITY, OverlayOpacityPolicy.DEFAULT_MASTER_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupMasterOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_MASTER_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupSurfaceOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_SURFACE_OPACITY, OverlayOpacityPolicy.DEFAULT_SURFACE_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupSurfaceOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_SURFACE_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupSnippetsOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_SNIPPETS_OPACITY, OverlayOpacityPolicy.DEFAULT_SNIPPETS_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupSnippetsOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_SNIPPETS_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupChromeOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_CHROME_OPACITY, OverlayOpacityPolicy.DEFAULT_CHROME_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupChromeOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_CHROME_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupCloseOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_CLOSE_OPACITY, OverlayOpacityPolicy.DEFAULT_CLOSE_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupCloseOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_CLOSE_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    fun popupResizeOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_RESIZE_OPACITY, OverlayOpacityPolicy.DEFAULT_RESIZE_OPACITY)
        .coerceIn(0f, 1f)

    fun setPopupResizeOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_POPUP_RESIZE_OPACITY, opacity.coerceIn(0f, 1f))
            .apply()
    }

    // --- Snippet Shadow / Clarity ---

    fun snippetShadowStrength(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_SNIPPET_SHADOW_STRENGTH, OverlayOpacityPolicy.DEFAULT_SHADOW_STRENGTH)
        .coerceIn(0f, 1f)

    fun setSnippetShadowStrength(context: Context, strength: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SNIPPET_SHADOW_STRENGTH, strength.coerceIn(0f, 1f))
            .apply()
    }

    /**
     * Resets visual appearance (all layer opacities, shadow, and bubble size) to default values.
     * Preserves positions, dimensions, filters, snippets, and all other settings.
     */
    fun resetAppearance(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_OPACITY, OverlayOpacityPolicy.DEFAULT_BUBBLE_OPACITY)
            .putFloat(KEY_POPUP_MASTER_OPACITY, OverlayOpacityPolicy.DEFAULT_MASTER_OPACITY)
            .putFloat(KEY_POPUP_SURFACE_OPACITY, OverlayOpacityPolicy.DEFAULT_SURFACE_OPACITY)
            .putFloat(KEY_POPUP_SNIPPETS_OPACITY, OverlayOpacityPolicy.DEFAULT_SNIPPETS_OPACITY)
            .putFloat(KEY_POPUP_CHROME_OPACITY, OverlayOpacityPolicy.DEFAULT_CHROME_OPACITY)
            .putFloat(KEY_POPUP_CLOSE_OPACITY, OverlayOpacityPolicy.DEFAULT_CLOSE_OPACITY)
            .putFloat(KEY_POPUP_RESIZE_OPACITY, OverlayOpacityPolicy.DEFAULT_RESIZE_OPACITY)
            .putFloat(KEY_SNIPPET_SHADOW_STRENGTH, OverlayOpacityPolicy.DEFAULT_SHADOW_STRENGTH)
            .putFloat(KEY_BUBBLE_SIZE_DP, DEFAULT_BUBBLE_SIZE_DP)
            .apply()
    }

    /**
     * Applies an [OverlayAppearancePreset] atomically: all layer opacities
     * plus shadow land in a single preferences edit.
     */
    fun applyAppearancePreset(context: Context, preset: OverlayAppearancePreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_OPACITY, OverlayOpacityPolicy.clamp(preset.bubble))
            .putFloat(KEY_POPUP_MASTER_OPACITY, OverlayOpacityPolicy.clamp(preset.master))
            .putFloat(KEY_POPUP_SURFACE_OPACITY, OverlayOpacityPolicy.clamp(preset.surface))
            .putFloat(KEY_POPUP_SNIPPETS_OPACITY, OverlayOpacityPolicy.clamp(preset.snippets))
            .putFloat(KEY_POPUP_CHROME_OPACITY, OverlayOpacityPolicy.clamp(preset.chrome))
            .putFloat(KEY_POPUP_CLOSE_OPACITY, OverlayOpacityPolicy.clamp(preset.close))
            .putFloat(KEY_POPUP_RESIZE_OPACITY, OverlayOpacityPolicy.clamp(preset.resize))
            .putFloat(KEY_SNIPPET_SHADOW_STRENGTH, OverlayOpacityPolicy.clamp(preset.shadow))
            .apply()
    }

    // --- Panel Geometry ---

    fun panelWidthPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_WIDTH, -1)

    fun setPanelWidthPx(context: Context, width: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_WIDTH, width)
            .apply()
    }

    fun panelHeightPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_HEIGHT, -1)

    fun setPanelHeightPx(context: Context, height: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_HEIGHT, height)
            .apply()
    }

    fun panelPositionX(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_X, -1)

    fun panelPositionY(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_Y, -1)

    fun setPanelPosition(context: Context, x: Int, y: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_X, x)
            .putInt(KEY_PANEL_Y, y)
            .apply()
    }

    // --- Visibility ---

    fun showTitle(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_TITLE, true)

    fun setShowTitle(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_TITLE, show)
            .apply()
    }

    fun showSearch(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_SEARCH, true)

    fun setShowSearch(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_SEARCH, show)
            .apply()
    }

    fun showCategories(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_CATEGORIES, true)

    fun setShowCategories(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_CATEGORIES, show)
            .apply()
    }

    // --- Start Filter ---

    fun startFilterMode(context: Context): OverlayStartFilterMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_START_FILTER_MODE, OverlayStartFilterMode.ALL.id)
        return OverlayStartFilterMode.fromId(raw)
    }

    fun setStartFilterMode(context: Context, mode: OverlayStartFilterMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_START_FILTER_MODE, mode.id)
            .apply()
    }

    fun startCustomCategory(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_START_CUSTOM_CATEGORY, "")
            .orEmpty()
    }

    fun setStartCustomCategory(context: Context, category: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_START_CUSTOM_CATEGORY, category.trim())
            .apply()
    }

    fun lastUsedFilter(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_USED_FILTER, null)
    }

    fun setLastUsedFilter(context: Context, filter: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_USED_FILTER, filter.trim())
            .apply()
    }

    // --- After Copy ---

    fun afterCopyAction(context: Context): OverlayAfterCopyAction {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AFTER_COPY_ACTION, OverlayAfterCopyAction.CLOSE_POPUP.id)
        return OverlayAfterCopyAction.fromId(raw)
    }

    fun setAfterCopyAction(context: Context, action: OverlayAfterCopyAction) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AFTER_COPY_ACTION, action.id)
            .apply()
    }

    // --- Pinned Snippets ---
    fun pinnedSnippetIds(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet("pinned_snippet_ids", emptySet()) ?: emptySet()
    }

    fun setPinnedSnippetIds(context: Context, ids: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet("pinned_snippet_ids", ids)
            .apply()
    }

    fun isSnippetPinned(context: Context, snippetId: Long): Boolean {
        return pinnedSnippetIds(context).contains(snippetId.toString())
    }

    fun toggleSnippetPinned(context: Context, snippetId: Long): Boolean {
        val current = pinnedSnippetIds(context).toMutableSet()
        val idStr = snippetId.toString()
        val pinned = if (current.contains(idStr)) {
            current.remove(idStr)
            false
        } else {
            current.add(idStr)
            true
        }
        setPinnedSnippetIds(context, current)
        return pinned
    }
}

