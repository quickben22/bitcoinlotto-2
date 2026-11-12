package com.quickben22.bitcoinlotto.ui.components

import java.text.NumberFormat
import java.util.Locale

fun formatCount(value: Long): String =
    NumberFormat.getIntegerInstance(Locale.US).format(value)

fun formatDuration(elapsedMillis: Long): String {
    val totalSeconds = (elapsedMillis / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/**
 * Splits a 64-character private key into readable lines so it fits on narrow screens.
 * A 32-byte key becomes two lines of 32 hex characters.
 */
fun formatPrivateKey(hex: String, groupSize: Int = 32): String =
    if (hex.isEmpty()) hex else hex.chunked(groupSize).joinToString("\n")

fun formatSpeed(keysPerSecond: Double): String = when {
    keysPerSecond >= 1_000_000.0 ->
        String.format(Locale.US, "%.1fM", keysPerSecond / 1_000_000.0)
    keysPerSecond >= 1_000.0 ->
        String.format(Locale.US, "%.1fk", keysPerSecond / 1_000.0)
    else -> String.format(Locale.US, "%.0f", keysPerSecond)
}
