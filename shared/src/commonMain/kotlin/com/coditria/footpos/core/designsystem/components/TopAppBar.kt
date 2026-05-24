package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

/**
 * Standard app bar: small back glyph + title, optional trailing slot.
 * Stays flush against the primary background — no shadow / divider — so it reads
 * like a continuation of the page rather than a Material 1 chrome header.
 */
@Composable
fun PosTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = PosTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconChip(glyph = "‹", onClick = onBack)
        } else {
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }
        Text(
            title,
            style = PosTheme.typography.title3,
            color = colors.labelPrimary,
            modifier = Modifier.padding(horizontal = 8.dp).weight(1f),
        )
        if (trailing != null) trailing()
    }
}

/**
 * Square, soft-fill chip used for icon-only actions (back, sync, etc.) in headers.
 * Cleaner than a bare glyph; gives a clear tap target without competing with primary
 * buttons.
 */
@Composable
fun IconChip(
    glyph: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    Box(
        modifier
            .size(40.dp)
            .clip(PosTheme.shapes.md)
            .background(colors.backgroundSecondary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = PosTheme.typography.title3, color = colors.labelPrimary)
    }
}

/** "STORE", "HARDWARE", "ABOUT" eyebrow above grouped settings cards. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    Text(
        text.uppercase(),
        style = PosTheme.typography.label,
        color = colors.labelTertiary,
        modifier = modifier.padding(start = 4.dp),
    )
}

/** Floating action bar pinned to the bottom (cart, checkout summary). */
@Composable
fun ActionBar(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    content: @Composable () -> Unit,
) {
    val colors = PosTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = horizontalArrangement) {
            content()
        }
    }
}
