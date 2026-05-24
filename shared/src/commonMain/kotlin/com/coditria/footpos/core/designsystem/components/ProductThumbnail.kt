package com.coditria.footpos.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Product

/**
 * Three-step fallback chain — remote URL → emoji → first letter — with a soft
 * shimmer placeholder during load and a clean fallback glyph on error. The
 * placeholder is a slow horizontal gradient sweep, which reads as "loading" to
 * users without needing a spinner cluttering the grid.
 */
@Composable
fun ProductThumbnail(
    product: Product,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    val url = product.imageUrl?.takeIf { it.isNotBlank() }

    Box(
        modifier = modifier.background(colors.backgroundSecondary),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            FallbackGlyph(product)
            return@Box
        }
        SubcomposeAsyncImage(
            model = url,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { ShimmerPlaceholder() },
            error = { FallbackGlyph(product) },
        )
    }
}

@Composable
private fun FallbackGlyph(product: Product) {
    val colors = PosTheme.colors
    Text(
        text = product.emoji ?: product.name.take(1).uppercase(),
        style = PosTheme.typography.largeTitle,
        color = colors.labelSecondary,
    )
}

@Composable
private fun ShimmerPlaceholder() {
    val colors = PosTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-x",
    )
    val base = colors.backgroundSecondary
    val highlight = colors.backgroundTertiary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                // Slide a soft highlight band from left to right.
                val bandWidth = size.width * 0.55f
                val x = -bandWidth + (size.width + bandWidth) * progress
                val brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(x, 0f),
                    end = Offset(x + bandWidth, size.height),
                )
                onDrawBehind { drawRect(brush = brush) }
            }
            .background(Color.Transparent),
    )
}
