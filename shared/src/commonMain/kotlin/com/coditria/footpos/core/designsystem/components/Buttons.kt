package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import androidx.compose.foundation.clickable as foundationClickable

/**
 * Buttons follow the mono + one-accent system:
 *   - Primary: filled with brand accent, white label
 *   - Secondary: outlined, ink label
 *   - Tertiary: text-only
 *
 * All three respond to press with a subtle scale (0.97x) for tactility, and
 * gracefully drop interaction when disabled / loading.
 */

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = PosTheme.colors
    val bg = if (enabled && !loading) colors.accent else colors.accent.copy(alpha = 0.45f)
    PressableSurface(
        modifier = modifier,
        shape = PosTheme.shapes.md,
        background = bg,
        enabled = enabled && !loading,
        onClick = onClick,
        minHeight = 52.dp,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = colors.onAccent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(text = text, style = PosTheme.typography.headline, color = colors.onAccent)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = PosTheme.colors
    PressableSurface(
        modifier = modifier
            .border(width = 1.dp, color = colors.separatorStrong, shape = PosTheme.shapes.md),
        shape = PosTheme.shapes.md,
        background = colors.backgroundPrimary,
        enabled = enabled,
        onClick = onClick,
        minHeight = 48.dp,
    ) {
        Text(text = text, style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}

@Composable
fun TertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = PosTheme.colors
    val color = if (destructive) colors.destructive else colors.labelPrimary
    PressableSurface(
        modifier = modifier,
        shape = PosTheme.shapes.md,
        background = Color.Transparent,
        enabled = enabled,
        onClick = onClick,
        minHeight = 44.dp,
    ) {
        Text(text = text, style = PosTheme.typography.body, color = color)
    }
}

@Composable
fun ButtonSpacer() {
    Spacer(Modifier.size(12.dp))
}

// ---- Internal: shared press-scale surface ----------------------------------

@Composable
internal fun PressableSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = PosTheme.shapes.md,
    background: Color = Color.Transparent,
    enabled: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 44.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = PosMotion.tweenFast(),
        label = "press-scale",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(background)
            .foundationClickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
