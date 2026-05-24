package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes

/**
 * Subtly-bordered card. Modern minimal eCommerce uses thin separators instead of
 * drop shadows — feels more architectural and ages better in dark mode.
 */
@Composable
fun PosCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = PosTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.surfaceElevated)
            .border(width = 1.dp, color = colors.separator, shape = PosTheme.shapes.lg),
    ) {
        content()
    }
}

/** Variant without border — used for hero sections / colored summary blocks. */
@Composable
fun PosTintCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(20.dp),
    content: @Composable () -> Unit,
) {
    val colors = PosTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.backgroundSecondary)
            .padding(contentPadding),
    ) {
        content()
    }
}
