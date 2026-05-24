package com.coditria.footpos.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography

@Composable
fun SplashScreen() {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .warmGradientCorner(accent = colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SpinningMonogram()
            Spacer(Modifier.size(24.dp))
            Text("Sahm Food", style = PosTheme.typography.display, color = colors.labelPrimary)
            Spacer(Modifier.size(6.dp))
            Text(
                "POINT OF SALE",
                style = PosTheme.typography.label,
                color = colors.labelTertiary,
            )
        }
    }
}

@Composable
private fun SpinningMonogram() {
    val colors = PosTheme.colors
    val transition = rememberInfiniteTransition(label = "monogram")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "monogram-rot",
    )
    Box(
        modifier = Modifier
            .size(80.dp)
            .rotate(angle)
            .clip(PosTheme.shapes.lg)
            .background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        // Counter-rotate so the letter stays upright while the tile spins.
        Text(
            "S",
            style = PosTheme.typography.display,
            color = colors.onAccent,
            modifier = Modifier.rotate(-angle),
        )
    }
}

/**
 * Soft radial wash of the accent in the top-right corner — gives the surface warmth
 * without painting the whole screen. Matches the auth-screen gradient so the launch →
 * auth handoff feels continuous.
 */
private fun Modifier.warmGradientCorner(accent: Color): Modifier = this.drawWithCache {
    val brush = Brush.radialGradient(
        colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
        center = Offset(size.width * 0.95f, size.height * 0.10f),
        radius = size.minDimension * 0.85f,
    )
    onDrawBehind { drawRect(brush) }
}
