package com.snapsave.app.service

/**
 * Opt-in presets only change appearance (opacities, shadow), never size, position,
 * filters or user snippet content. Direct 1:1 parity with StickHub.
 */
enum class OverlayAppearancePreset(
    val title: String,
    val description: String,
    val bubble: Float,
    val master: Float,
    val surface: Float,
    val snippets: Float,
    val chrome: Float,
    val close: Float,
    val resize: Float,
    val shadow: Float
) {
    BALANCED(
        title = "Balanced",
        description = "Solid contrast with discreet controls",
        bubble = 1.0f,
        master = 1.0f,
        surface = 0.96f,
        snippets = 1.0f,
        chrome = 1.0f,
        close = 1.0f,
        resize = 0.85f,
        shadow = 0.45f
    ),
    FLOATING(
        title = "Floating snippets",
        description = "No panel background; full-strength snippets",
        bubble = 0.75f,
        master = 1.0f,
        surface = 0.0f,
        snippets = 1.0f,
        chrome = 0.0f,
        close = 0.65f,
        resize = 0.55f,
        shadow = 0.65f
    ),
    DISCREET(
        title = "Discreet",
        description = "A softer panel without dimming your snippets",
        bubble = 0.5f,
        master = 1.0f,
        surface = 0.65f,
        snippets = 1.0f,
        chrome = 0.85f,
        close = 0.75f,
        resize = 0.6f,
        shadow = 0.35f
    )
}
