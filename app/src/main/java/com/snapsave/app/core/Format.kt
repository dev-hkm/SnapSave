package com.snapsave.app.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatSize(bytes: Long): String = when {
    bytes < 1_000 -> "$bytes B"
    bytes < 1_000_000 -> String.format(Locale.US, "%.1f KB", bytes / 1_000.0)
    else -> String.format(Locale.US, "%.2f MB", bytes / 1_000_000.0)
}

fun relativeTime(ts: Long, s: AppStrings = StringsEn): String {
    val diff = System.currentTimeMillis() - ts
    val minutes = diff / 60_000
    return when {
        diff < 45_000 -> s.justNow
        minutes < 60 -> s.minutesAgo(minutes)
        minutes < 60 * 24 -> s.hoursAgo(minutes / 60)
        minutes < 60 * 48 -> s.yesterday
        else -> SimpleDateFormat(if (s is StringsVi) "dd/MM/yyyy" else "MMM dd, yyyy", Locale.getDefault()).format(Date(ts))
    }
}

fun formatDateTime(ts: Long): String =
    SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault()).format(Date(ts))
