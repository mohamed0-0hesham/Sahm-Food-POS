package com.coditria.footpos.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4pt base unit, matching Apple's HIG spacing scale. */
@Immutable
data class PosSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 40.dp,
)

val DefaultPosSpacing = PosSpacing()

val LocalPosSpacing = staticCompositionLocalOf { DefaultPosSpacing }

val PosTheme.spacing: PosSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalPosSpacing.current
