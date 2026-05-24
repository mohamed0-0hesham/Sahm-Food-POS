package com.coditria.footpos.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tightened modern eCommerce type scale.
 *
 * Display sizes are heavier and use slightly negative tracking for premium feel.
 * Body text uses Medium weight as the new default — modern brands (Linear, Vercel,
 * Shopify) have moved away from Regular body because it looks dated on hi-DPI screens.
 * Numeric styles enable tabular-nums so prices align in tables and grids.
 */
@Immutable
data class PosTypography(
    val display: TextStyle,
    val largeTitle: TextStyle,
    val title1: TextStyle,
    val title2: TextStyle,
    val title3: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val callout: TextStyle,
    val subhead: TextStyle,
    val footnote: TextStyle,
    val caption1: TextStyle,
    val caption2: TextStyle,
    val mono: TextStyle,
    val label: TextStyle,
)

val DefaultPosTypography = PosTypography(
    display = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, lineHeight = 48.sp, letterSpacing = (-1).sp),
    largeTitle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    title1 = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp, letterSpacing = (-0.4).sp, fontFeatureSettings = "tnum"),
    title2 = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp, letterSpacing = (-0.3).sp, fontFeatureSettings = "tnum"),
    title3 = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    headline = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp, fontFeatureSettings = "tnum"),
    body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    callout = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
    subhead = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, fontFeatureSettings = "tnum"),
    footnote = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),
    caption1 = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    caption2 = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp),
    mono = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp),
    // Uppercase label used for section eyebrows ("STORE", "ABOUT"). Wide tracking.
    label = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp, letterSpacing = 1.2.sp),
)

val LocalPosTypography = staticCompositionLocalOf { DefaultPosTypography }

val PosTheme.typography: PosTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalPosTypography.current
