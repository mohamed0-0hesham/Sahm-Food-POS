package com.coditria.footpos.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Minimal monochrome system with a single bold accent.
 *
 * The neutral ramp is a tight tonal scale (zinc / true greys) — every surface and
 * label comes from it. The accent is the only colored hue in the product UI, used
 * sparingly for primary actions, price callouts, focused states, and badges. This
 * mono + one approach is what gives modern eCommerce stores (Aritzia, Acne, SSENSE,
 * Vercel, Linear) their confident, premium feel.
 *
 * Semantic colors (success / warning / destructive) stay outside the brand palette
 * and are used only for status conveyance, never decoration.
 */
@Immutable
data class PosColors(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val surfaceElevated: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val labelTertiary: Color,
    val labelInverted: Color,
    val separator: Color,
    val separatorStrong: Color,
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val destructive: Color,
    val isDark: Boolean,
)

// Brand accent — single bold modern-orange. Modern, distinctive, food-friendly.
// Pull-quote: "warm without being garish." Same hue across light/dark.
private val AccentLight = Color(0xFFFF5A1F)
private val AccentDark = Color(0xFFFF7A47)

val LightPosColors = PosColors(
    backgroundPrimary = Color(0xFFFFFFFF),
    backgroundSecondary = Color(0xFFFAFAFA),
    backgroundTertiary = Color(0xFFF4F4F5),
    surfaceElevated = Color(0xFFFFFFFF),
    labelPrimary = Color(0xFF09090B),
    labelSecondary = Color(0xFF52525B),
    labelTertiary = Color(0xFFA1A1AA),
    labelInverted = Color(0xFFFAFAFA),
    separator = Color(0xFFE4E4E7),
    separatorStrong = Color(0xFFD4D4D8),
    accent = AccentLight,
    onAccent = Color(0xFFFFFFFF),
    success = Color(0xFF16A34A),
    warning = Color(0xFFEAB308),
    destructive = Color(0xFFDC2626),
    isDark = false,
)

val DarkPosColors = PosColors(
    backgroundPrimary = Color(0xFF09090B),
    backgroundSecondary = Color(0xFF18181B),
    backgroundTertiary = Color(0xFF27272A),
    surfaceElevated = Color(0xFF1F1F22),
    labelPrimary = Color(0xFFFAFAFA),
    labelSecondary = Color(0xFFA1A1AA),
    labelTertiary = Color(0xFF71717A),
    labelInverted = Color(0xFF09090B),
    separator = Color(0xFF27272A),
    separatorStrong = Color(0xFF3F3F46),
    accent = AccentDark,
    onAccent = Color(0xFF09090B),
    success = Color(0xFF22C55E),
    warning = Color(0xFFFACC15),
    destructive = Color(0xFFEF4444),
    isDark = true,
)

val LocalPosColors = staticCompositionLocalOf { LightPosColors }

object PosTheme {
    val colors: PosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPosColors.current
}
