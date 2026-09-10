package com.snapsave.app.core

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

enum class HapticKind { TICK, CLICK, LONG, CONFIRM, REJECT }

/** Công tắc rung toàn cục, đồng bộ từ Cài đặt (DataStore). */
object AppHaptics {
    @Volatile
    var enabled: Boolean = true
}

fun View.haptic(kind: HapticKind) {
    if (!AppHaptics.enabled) return
    val constant = when (kind) {
        HapticKind.TICK -> HapticFeedbackConstants.CLOCK_TICK
        HapticKind.CLICK -> HapticFeedbackConstants.KEYBOARD_TAP
        HapticKind.LONG -> HapticFeedbackConstants.LONG_PRESS
        HapticKind.CONFIRM ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
        HapticKind.REJECT ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(constant)
}
