package com.quickben22.bitcoinlotto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFFB3FFFF)
val Background = Color(0xFF000000)
val Surface = Color(0xFF0B0F0F)
val ErrorRed = Color(0xFFFF5555)

private val BitcoinLottoColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color.Black,
    secondary = Cyan,
    onSecondary = Color.Black,
    background = Background,
    onBackground = Cyan,
    surface = Surface,
    onSurface = Cyan,
    surfaceVariant = Surface,
    onSurfaceVariant = Cyan,
    error = ErrorRed,
    onError = Color.Black,
)

@Composable
fun BitcoinLottoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BitcoinLottoColors,
        typography = Typography(),
        content = content,
    )
}
