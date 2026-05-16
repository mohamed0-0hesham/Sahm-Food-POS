package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.SyncStatus

@Composable
fun SyncStatusIcon(status: SyncStatus, modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    when (status) {
        SyncStatus.SYNCED -> StatusGlyph("✓", colors.success, modifier)
        SyncStatus.PENDING -> RotatingGlyph("↻", colors.warning, modifier)
        SyncStatus.FAILED -> StatusGlyph("⚠", colors.destructive, modifier)
    }
}

@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    Row(modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("⌀", color = colors.labelSecondary, style = PosTheme.typography.subhead)
    }
}

@Composable
private fun StatusGlyph(text: String, tint: Color, modifier: Modifier) {
    Text(text, style = PosTheme.typography.subhead, color = tint, modifier = modifier)
}

@Composable
private fun RotatingGlyph(text: String, tint: Color, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "sync-rotate")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Text(text, style = PosTheme.typography.subhead, color = tint, modifier = modifier.rotate(angle))
}
