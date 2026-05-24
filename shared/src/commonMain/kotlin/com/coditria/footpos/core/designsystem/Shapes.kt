package com.coditria.footpos.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Modern minimal corner radii. Sharper than iOS-default — closer to the 8/12/16/24
 * progression you see across Vercel / Linear / Shopify Polaris.
 */
@Immutable
data class PosShapes(
    val xs: RoundedCornerShape = RoundedCornerShape(6.dp),
    val sm: RoundedCornerShape = RoundedCornerShape(8.dp),
    val md: RoundedCornerShape = RoundedCornerShape(12.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(16.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
)

val DefaultPosShapes = PosShapes()

val LocalPosShapes = staticCompositionLocalOf { DefaultPosShapes }

val PosTheme.shapes: PosShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalPosShapes.current
