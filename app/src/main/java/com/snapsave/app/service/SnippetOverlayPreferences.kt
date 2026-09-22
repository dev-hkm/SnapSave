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

/**
 * Persisted preferences for SnapSave's floating quick-snippet overlay and popup.
 * Replicated 1:1 with StickHub's OverlayPreferences.
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

    private const val KEY_AFTER_COPY_ACTION = "after_copy_action"
    private const val KEY_LAST_USED_FILTER = "last_used_filter"

    const val DEFAULT_BUBBLE_SIZE_DP = 46f
    const val MIN_BUBBLE_SIZE_DP = 36f
    const val MAX_BUBBLE_SIZE_DP = 72f

    const val DEFAULT_BUBBLE_FRACTION_X = 0.94f
    const val DEFAULT_BUBBLE_FRACTION_Y = 0.35f

    const val DEFAULT_BUBBLE_OPACITY = 0.92f
    const val DEFAULT_MASTER_OPACITY = 1.0f
    const val DEFAULT_SURFACE_OPACITY = 0.95f
    const val DEFAULT_SNIPPETS_OPACITY = 1.0f
    const val DEFAULT_CHROME_OPACITY = 1.0f
    const val DEFAULT_CLOSE_OPACITY = 0.95f
    const val DEFAULT_RESIZE_OPACITY = 0.85f

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
        .getFloat(KEY_BUBBLE_OPACITY, DEFAULT_BUBBLE_OPACITY)
        .coerceIn(0.1f, 1f)

    fun setBubbleOpacity(context: Context, opacity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_OPACITY, opacity.coerceIn(0.1f, 1f))
            .apply()
    }

    fun popupMasterOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_MASTER_OPACITY, DEFAULT_MASTER_OPACITY)
        .coerceIn(0.1f, 1f)

    fun popupSurfaceOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_SURFACE_OPACITY, DEFAULT_SURFACE_OPACITY)
        .coerceIn(0.1f, 1f)

    fun popupSnippetsOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_SNIPPETS_OPACITY, DEFAULT_SNIPPETS_OPACITY)
        .coerceIn(0.1f, 1f)

    fun popupChromeOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_CHROME_OPACITY, DEFAULT_CHROME_OPACITY)
        .coerceIn(0.1f, 1f)

    fun popupCloseOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_CLOSE_OPACITY, DEFAULT_CLOSE_OPACITY)
        .coerceIn(0.1f, 1f)

    fun popupResizeOpacity(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_POPUP_RESIZE_OPACITY, DEFAULT_RESIZE_OPACITY)
        .coerceIn(0.1f, 1f)

    // --- Panel Bounds ---

    fun panelWidthPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_WIDTH, 0)

    fun setPanelWidthPx(context: Context, width: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_WIDTH, width)
            .apply()
    }

    fun panelHeightPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_HEIGHT, 0)

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
}
