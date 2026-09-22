package com.snapsave.app.ui.settings

/** Keeps direct manipulation independent of delayed parent/preference echoes. */
class SliderInteractionState(initialValue: Float, private val range: ClosedFloatingPointRange<Float>) {
    private var committed = sanitize(initialValue, range.start)
    var value = committed
        private set
    private var dragging = false

    fun change(next: Float): Float {
        dragging = true
        value = sanitize(next, value)
        return value
    }

    fun synchronize(next: Float) {
        if (!dragging) {
            committed = sanitize(next, committed)
            value = committed
        }
    }

    fun finish(): Float {
        dragging = false
        committed = value
        return value
    }

    fun cancel(): Float {
        dragging = false
        value = committed
        return value
    }

    private fun sanitize(next: Float, fallback: Float) =
        if (next.isFinite()) next.coerceIn(range.start, range.endInclusive) else fallback
}

/** UI-thread gate: no state reads in composition, no disk or binder on every pointer sample. */
class PreviewRateLimiter(private val intervalMs: Long = 32L) {
    private var previousLayer: String? = null
    private var lastDispatchMs = 0L

    fun shouldDispatch(layer: String, nowMs: Long): Boolean {
        if (previousLayer != layer || nowMs < lastDispatchMs || nowMs - lastDispatchMs >= intervalMs) {
            previousLayer = layer
            lastDispatchMs = nowMs
            return true
        }
        return false
    }

    fun reset() {
        previousLayer = null
    }
}
