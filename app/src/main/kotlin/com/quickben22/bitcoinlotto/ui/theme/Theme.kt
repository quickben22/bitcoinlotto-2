package com.quickben22.bitcoinlotto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cyan = Color(0xFFB3FFFF)
val Background = Color(0xFF000000)
val Surface = Color(0xFF0B0F0F)
val ErrorRed = Color(0xFFFF5555)
val SuccessGreen = Color(0xFF63E6A0)
val WarningAmber = Color(0xFFFFC46B)
val TextPrimary = Color(0xFFEAF7F7)
val TextMuted = Color(0xFF8FC4C4)

private val BitcoinLottoColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Color.Black,
    secondary = SuccessGreen,
    onSecondary = Color.Black,
    tertiary = WarningAmber,
    onTertiary = Color.Black,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface,
    onSurfaceVariant = TextMuted,
    error = ErrorRed,
    onError = Color.Black,
)

private val DefaultTypography = Typography()

private val AppTypography = Typography(
    headlineMedium = DefaultTypography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = DefaultTypography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = DefaultTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = DefaultTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = DefaultTypography.bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = DefaultTypography.bodyMedium.copy(lineHeight = 20.sp),
    labelLarge = DefaultTypography.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.4.sp,
    ),
    labelMedium = DefaultTypography.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
    ),
    labelSmall = DefaultTypography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp,
    ),
)

@Composable
fun BitcoinLottoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BitcoinLottoColors,
        typography = AppTypography,
        content = content,
    )
}
