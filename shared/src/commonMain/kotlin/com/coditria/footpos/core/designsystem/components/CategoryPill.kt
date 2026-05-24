package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

/**
 * Selected state inverts to filled-ink instead of accent fill — keeps the brand
 * accent reserved for primary actions, while the selection still reads confidently.
 * Colors animate so taps feel deliberate.
 */
@Composable
fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors

    val background by animateColorAsState(
        targetValue = if (selected) colors.labelPrimary else colors.backgroundSecondary,
        animationSpec = PosMotion.tweenStandard(),
        label = "pill-bg",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) colors.labelInverted else colors.labelSecondary,
        animationSpec = PosMotion.tweenStandard(),
        label = "pill-fg",
    )

    Box(
        modifier = modifier
            .clip(PosTheme.shapes.pill)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = PosTheme.typography.subhead, color = foreground)
    }
}
