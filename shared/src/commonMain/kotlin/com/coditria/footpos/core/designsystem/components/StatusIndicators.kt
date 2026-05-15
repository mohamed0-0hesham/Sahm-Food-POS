package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.domain.model.SyncStatus

@Composable
fun SyncStatusIcon(status: SyncStatus, modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    when (status) {
        SyncStatus.SYNCED -> IconBadge(Icons.Rounded.CheckCircle, colors.success, modifier)
        SyncStatus.PENDING -> RotatingIcon(Icons.Rounded.Refresh, colors.warning, modifier)
        SyncStatus.FAILED -> IconBadge(Icons.Rounded.WarningAmber, colors.destructive, modifier)
    }
}

@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    Row(modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CloudOff, contentDescription = "Offline", tint = colors.labelSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Icon(icon, contentDescription = null, tint = tint, modifier = modifier.size(16.dp))
}

@Composable
private fun RotatingIcon(icon: ImageVector, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) {
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
    Icon(icon, contentDescription = null, tint = tint, modifier = modifier.size(16.dp).rotate(angle))
}
