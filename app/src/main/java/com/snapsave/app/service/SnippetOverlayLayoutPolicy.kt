package com.snapsave.app.service

import kotlin.math.max

/**
 * Pure math and layout rules for the quick-snippets floating overlay.
 * Replicated with exact fidelity from StickHub's OverlayLayoutPolicy.
 */
object SnippetOverlayLayoutPolicy {
    // Default sizing tokens in DP
    const val DEFAULT_PANEL_WIDTH_DP = 330f
    const val DEFAULT_PANEL_HEIGHT_DP = 420f

    const val MIN_PANEL_WIDTH_DP = 260f
    const val MIN_CARD_HEIGHT_DP = 70f
    const val CELL_MARGIN_DP = 4f

    const val CHROME_TITLE_HEIGHT_DP = 40f
    const val CHROME_SEARCH_HEIGHT_DP = 46f
    const val CHROME_CATEGORIES_HEIGHT_DP = 38f

    // Keep the close control compact; its touch target sits outside the panel
    const val CLOSE_CONTROL_SIZE_DP = 32f
    const val CLOSE_DOCK_OFFSET_DP = 8f

    data class PanelBounds(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    /** Position of the compact close control in its own overlay window. */
    data class CloseOverlayPosition(val x: Int, val y: Int)

    /**
     * Unified layout snapshot capturing panel bounds, outside close button position,
     * bounds limits, and responsive layout.
     */
    data class PopupGeometry(
        val panelBounds: PanelBounds,
        val closePosition: CloseOverlayPosition,
        val minWidth: Int,
        val minHeight: Int,
        val maxWidth: Int,
        val maxHeight: Int
    )

    /**
     * Dock the close control on the outside corner of the popup. The center
     * sits on the popup's top-right corner, leaving half of the control above
     * and to the right of the surface instead of consuming content space.
     */
    fun closeOverlayPosition(
        panelX: Int,
        panelY: Int,
        panelWidth: Int,
        closeSize: Int,
        screenWidth: Int,
        screenHeight: Int,
        offsetPx: Int = CLOSE_DOCK_OFFSET_DP.toInt()
    ): CloseOverlayPosition {
        val maxX = max(0, screenWidth - closeSize)
        val maxY = max(0, screenHeight - closeSize)
        return CloseOverlayPosition(
            x = (panelX + panelWidth + offsetPx - closeSize / 2).coerceIn(0, maxX),
            y = (panelY - offsetPx - closeSize / 2).coerceIn(0, maxY)
        )
    }

    /**
     * Minimum width required for the panel.
     */
    fun minPanelWidthPx(density: Float): Int {
        return max(1, (MIN_PANEL_WIDTH_DP * density).toInt())
    }

    /**
     * Extra vertical height required by the enabled chrome elements.
     */
    fun chromeHeightPx(
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean
    ): Int {
        var chromeDp = 0f
        if (showTitle) chromeDp += CHROME_TITLE_HEIGHT_DP
        if (showSearch) chromeDp += CHROME_SEARCH_HEIGHT_DP
        if (showCategories) chromeDp += CHROME_CATEGORIES_HEIGHT_DP
        return (chromeDp * density).toInt()
    }

    /**
     * Minimum height required: at least 2 snippet cards + enabled chrome.
     */
    fun minPanelHeightPx(
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean,
        verticalPaddingDp: Float = 8f
    ): Int {
        val minCardsDp = (2 * MIN_CARD_HEIGHT_DP) + (verticalPaddingDp * 2)
        val chromePx = chromeHeightPx(density, showTitle, showSearch, showCategories)
        return (minCardsDp * density).toInt() + chromePx
    }

    /**
     * Computes the complete immutable geometry snapshot for the quick snippets popup.
     */
    fun computePopupGeometry(
        requestedX: Int,
        requestedY: Int,
        requestedWidth: Int,
        requestedHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        density: Float,
        showTitle: Boolean,
        showSearch: Boolean,
        showCategories: Boolean,
        closeButtonSizePx: Int,
        closeOffsetPx: Int = 4
    ): PopupGeometry {
        val minW = minPanelWidthPx(density)
        val minH = minPanelHeightPx(density, showTitle, showSearch, showCategories)
        val maxW = (screenWidth * 0.94f).toInt()
        val maxH = (screenHeight * 0.85f).toInt()
        val panelBounds = clampPanelBounds(
            x = requestedX,
            y = requestedY,
            width = requestedWidth,
            height = requestedHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            minWidth = minW,
            minHeight = minH,
            maxWidth = maxW,
            maxHeight = maxH
        )
        val closePos = closeOverlayPosition(
            panelX = panelBounds.x,
            panelY = panelBounds.y,
            panelWidth = panelBounds.width,
            closeSize = closeButtonSizePx,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            offsetPx = closeOffsetPx
        )
        return PopupGeometry(
            panelBounds = panelBounds,
            closePosition = closePos,
            minWidth = minW,
            minHeight = minH,
            maxWidth = maxW,
            maxHeight = maxH
        )
    }

    /**
     * Clamps panel size and coordinates so the overlay is always 100% inside the visible screen.
     */
    fun clampPanelBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        minWidth: Int,
        minHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): PanelBounds {
        val capW = max(1, minOf(maxWidth, screenWidth))
        val capH = max(1, minOf(maxHeight, screenHeight))
        val effectiveMinW = minOf(max(1, minWidth), capW)
        val effectiveMinH = minOf(max(1, minHeight), capH)

        val clampedW = width.coerceIn(effectiveMinW, capW)
        val clampedH = height.coerceIn(effectiveMinH, capH)

        val maxX = max(0, screenWidth - clampedW)
        val maxY = max(0, screenHeight - clampedH)

        val clampedX = x.coerceIn(0, maxX)
        val clampedY = y.coerceIn(0, maxY)

        return PanelBounds(
            x = clampedX,
            y = clampedY,
            width = clampedW,
            height = clampedH
        )
    }

    data class NormalizedPosition(
        val fractionX: Float,
        val fractionY: Float
    )

    data class BubbleBounds(
        val x: Int,
        val y: Int,
        val size: Int,
        val maxX: Int,
        val maxY: Int
    )

    /**
     * Normalizes a pixel position relative to current maximum allowed coordinates (0..maxX, 0..maxY).
     */
    fun normalizePosition(x: Int, y: Int, maxX: Int, maxY: Int): NormalizedPosition {
        val safeMaxX = max(0, maxX)
        val safeMaxY = max(0, maxY)
        val fractionX = if (safeMaxX > 0) (x.toFloat() / safeMaxX).coerceIn(0f, 1f) else 0f
        val fractionY = if (safeMaxY > 0) (y.toFloat() / safeMaxY).coerceIn(0f, 1f) else 0f
        return NormalizedPosition(fractionX, fractionY)
    }

    /**
     * Denormalizes a relative fraction (0..1) back into pixel coordinates for new screen bounds.
     */
    fun denormalizePosition(normalized: NormalizedPosition, maxX: Int, maxY: Int): Pair<Int, Int> {
        val safeMaxX = max(0, maxX)
        val safeMaxY = max(0, maxY)
        val pxX = Math.round(normalized.fractionX.coerceIn(0f, 1f) * safeMaxX).toInt().coerceIn(0, safeMaxX)
        val pxY = Math.round(normalized.fractionY.coerceIn(0f, 1f) * safeMaxY).toInt().coerceIn(0, safeMaxY)
        return Pair(pxX, pxY)
    }

    /**
     * Clamps bubble size and coordinates so the bubble is guaranteed 100% inside screen bounds.
     */
    fun clampBubbleBounds(
        x: Int,
        y: Int,
        bubbleSize: Int,
        screenWidth: Int,
        screenHeight: Int
    ): BubbleBounds {
        val safeSize = max(1, minOf(bubbleSize, screenWidth, screenHeight))
        val maxX = max(0, screenWidth - safeSize)
        val maxY = max(0, screenHeight - safeSize)
        val clampedX = x.coerceIn(0, maxX)
        val clampedY = y.coerceIn(0, maxY)
        return BubbleBounds(
            x = clampedX,
            y = clampedY,
            size = safeSize,
            maxX = maxX,
            maxY = maxY
        )
    }

    fun clampBubbleSize(sizeDp: Float): Float {
        return sizeDp.coerceIn(
            SnippetOverlayPreferences.MIN_BUBBLE_SIZE_DP,
            SnippetOverlayPreferences.MAX_BUBBLE_SIZE_DP
        )
    }
}
