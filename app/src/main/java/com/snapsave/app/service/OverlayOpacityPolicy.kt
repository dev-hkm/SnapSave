package com.snapsave.app.service

/**
 * Pure Kotlin policy managing independent layer opacities and master popup visibility multiplier.
 * Replicated 1:1 from StickHub.
 */
object OverlayOpacityPolicy {
    const val DEFAULT_BUBBLE_OPACITY = 1.0f
    const val DEFAULT_MASTER_OPACITY = 1.0f
    const val DEFAULT_SURFACE_OPACITY = 0.96f
    const val DEFAULT_SNIPPETS_OPACITY = 1.0f
    const val DEFAULT_CHROME_OPACITY = 1.0f
    const val DEFAULT_CLOSE_OPACITY = 1.0f
    const val DEFAULT_RESIZE_OPACITY = 0.85f
    const val DEFAULT_SHADOW_STRENGTH = 0.45f

    /**
     * Corrupted preferences (NaN/±infinity) must recover to a visible value,
     * never propagate into window alpha. coerceIn alone passes NaN through.
     */
    fun clamp(value: Float): Float = if (!value.isFinite()) 1f else value.coerceIn(0f, 1f)

    fun effectiveSurfaceOpacity(master: Float, surface: Float): Float =
        clamp(master) * clamp(surface)

    fun effectiveSnippetsOpacity(master: Float, snippets: Float): Float =
        clamp(master) * clamp(snippets)

    fun effectiveChromeOpacity(master: Float, chrome: Float): Float =
        clamp(master) * clamp(chrome)

    fun effectiveCloseOpacity(master: Float, close: Float): Float =
        clamp(master) * clamp(close)

    fun effectiveResizeOpacity(master: Float, resize: Float): Float =
        clamp(master) * clamp(resize)
}
