package com.snapsave.app.service

/**
 * Ephemeral slider previews and the recovery reveal never change saved preferences.
 * Replicated 1:1 from StickHub.
 */
class OverlayAppearanceState {
    private val overrides = mutableMapOf<String, Float>()
    private var revealUntil = 0L

    fun preview(layer: String?, value: Float): Boolean {
        if (layer !in LAYERS || !value.isFinite()) return false
        overrides[layer!!] = value.coerceIn(0f, 1f)
        return true
    }

    fun clearPreviews() = overrides.clear()

    fun reveal(now: Long) {
        revealUntil = now + REVEAL_DURATION_MS
    }

    fun opacity(layer: String, committed: Float, now: Long): Float =
        if (now < revealUntil) 1f else overrides[layer] ?: committed

    companion object {
        const val REVEAL_DURATION_MS = 5_000L
        private val LAYERS = setOf("bubble", "master", "surface", "snippets", "chrome", "close", "resize")
    }
}
