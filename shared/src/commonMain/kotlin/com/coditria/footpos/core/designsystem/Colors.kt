package com.coditria.footpos.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PosColors(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val labelTertiary: Color,
    val separator: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val destructive: Color,
    val isDark: Boolean,
)

val LightPosColors = PosColors(
    backgroundPrimary = Color(0xFFFFFFFF),
    backgroundSecondary = Color(0xFFF2F2F7),
    backgroundTertiary = Color(0xFFFFFFFF),
    labelPrimary = Color(0xFF000000),
    labelSecondary = Color(0xFF3C3C43).copy(alpha = 0.60f),
    labelTertiary = Color(0xFF3C3C43).copy(alpha = 0.30f),
    separator = Color(0xFF3C3C43).copy(alpha = 0.29f),
    accent = Color(0xFF007AFF),
    success = Color(0xFF34C759),
    warning = Color(0xFFFF9500),
    destructive = Color(0xFFFF3B30),
    isDark = false,
)

val DarkPosColors = PosColors(
    backgroundPrimary = Color(0xFF000000),
    backgroundSecondary = Color(0xFF1C1C1E),
    backgroundTertiary = Color(0xFF2C2C2E),
    labelPrimary = Color(0xFFFFFFFF),
    labelSecondary = Color(0xFFEBEBF5).copy(alpha = 0.60f),
    labelTertiary = Color(0xFFEBEBF5).copy(alpha = 0.30f),
    separator = Color(0xFF545458).copy(alpha = 0.60f),
    accent = Color(0xFF0A84FF),
    success = Color(0xFF30D158),
    warning = Color(0xFFFF9F0A),
    destructive = Color(0xFFFF453A),
    isDark = true,
)

val LocalPosColors = staticCompositionLocalOf { LightPosColors }

object PosTheme {
    val colors: PosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPosColors.current
}
