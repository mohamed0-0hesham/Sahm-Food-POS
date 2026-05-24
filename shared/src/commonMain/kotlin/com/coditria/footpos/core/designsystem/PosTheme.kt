package com.coditria.footpos.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun PosAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkPosColors else LightPosColors

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.backgroundPrimary,
            onBackground = colors.labelPrimary,
            surface = colors.backgroundSecondary,
            onSurface = colors.labelPrimary,
            surfaceVariant = colors.backgroundTertiary,
            onSurfaceVariant = colors.labelSecondary,
            outline = colors.separator,
            error = colors.destructive,
            onError = colors.labelInverted,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.backgroundPrimary,
            onBackground = colors.labelPrimary,
            surface = colors.backgroundSecondary,
            onSurface = colors.labelPrimary,
            surfaceVariant = colors.backgroundTertiary,
            onSurfaceVariant = colors.labelSecondary,
            outline = colors.separator,
            error = colors.destructive,
            onError = colors.labelInverted,
        )
    }

    CompositionLocalProvider(
        LocalPosColors provides colors,
        LocalPosTypography provides DefaultPosTypography,
        LocalPosSpacing provides DefaultPosSpacing,
        LocalPosShapes provides DefaultPosShapes,
    ) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
}
