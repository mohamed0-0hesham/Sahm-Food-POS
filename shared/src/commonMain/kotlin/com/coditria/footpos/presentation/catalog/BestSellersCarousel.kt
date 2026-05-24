package com.coditria.footpos.presentation.catalog

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.ProductThumbnail
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Product
import kotlinx.coroutines.delay

/**
 * Auto-advancing hero pager that promotes the shop's bestsellers.
 *
 * Each card surfaces:
 *   • the rank pill ("#1 · 8 SOLD") so cashiers see the demand signal at a glance
 *   • an in-cart quantity badge ("×N") top-right, mirroring the grid below
 *   • the category eyebrow, name, accent price
 *   • an Add CTA overlay so the rush path is one tap
 *
 * Long-press still opens product detail. Auto-advance pauses while the carousel is
 * scrolling so the cashier never fights the animation when swiping manually.
 */
@Composable
fun BestSellersCarousel(
    sellers: List<Pair<Product, Int>>,
    cartQuantities: Map<String, Int>,
    /** Add the product to the cart (the small "+ Add" pill on the hero card). */
    onAdd: (Product) -> Unit,
    /** Tap on the hero image or anywhere else on the card opens the detail sheet. */
    onOpenDetail: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sellers.isEmpty()) return
    val colors = PosTheme.colors

    val pagerState = rememberPagerState(pageCount = { sellers.size })

    // Auto-advance every ~3.8s, but only when the user isn't actively scrolling.
    LaunchedEffect(pagerState, sellers.size) {
        while (sellers.size > 1) {
            delay(3_800)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % sellers.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier) {
        Row(
            Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "BEST SELLERS",
                style = PosTheme.typography.label,
                color = colors.labelTertiary,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${pagerState.currentPage + 1} / ${sellers.size}",
                style = PosTheme.typography.caption1,
                color = colors.labelTertiary,
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val (product, sold) = sellers[page]
            val qty = cartQuantities[product.id.value] ?: 0
            HeroCard(
                product = product,
                rank = page + 1,
                soldCount = sold,
                quantityInCart = qty,
                onAdd = { onAdd(product) },
                onOpenDetail = { onOpenDetail(product) },
            )
        }
        Spacer(Modifier.height(10.dp))
        DotIndicator(count = sellers.size, current = pagerState.currentPage)
    }
}

@Composable
private fun HeroCard(
    product: Product,
    rank: Int,
    soldCount: Int,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(PosTheme.shapes.lg)
            .background(colors.backgroundSecondary)
            // Tap the card → open detail. The accent + Add pill (below) is the
            // only surface that adds straight to the cart.
            .clickable(onClick = onOpenDetail),
    ) {
        ProductThumbnail(
            product = product,
            modifier = Modifier.fillMaxSize(),
        )
        // Soft bottom scrim — only enough to keep the overlay text legible. We
        // start the fade later (~55%) and end at a milder alpha (0.45) so the
        // product photo stays the hero rather than getting buried in darkness.
        Box(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                        startY = size.height * 0.55f,
                        endY = size.height,
                    )
                    onDrawBehind { drawRect(brush) }
                },
        )

        // Rank pill, top-left.
        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(PosTheme.shapes.pill)
                .background(Color.White.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("#$rank", style = PosTheme.typography.caption1, color = colors.accent)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(3.dp).clip(CircleShape).background(Color(0xFF71717A)))
            Spacer(Modifier.width(6.dp))
            Text(
                "$soldCount SOLD",
                style = PosTheme.typography.label,
                color = Color(0xFF09090B),
            )
        }

        // Quantity-in-cart badge, top-right.
        if (quantityInCart > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "×$quantityInCart",
                    style = PosTheme.typography.subhead,
                    color = Color.White,
                )
            }
        }

        // Bottom — category eyebrow, name, price + Add CTA pinned right.
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                product.category?.let {
                    Text(
                        it.uppercase(),
                        style = PosTheme.typography.label,
                        color = Color.White.copy(alpha = 0.80f),
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    product.name,
                    style = PosTheme.typography.title2,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    product.price.format(),
                    style = PosTheme.typography.headline,
                    color = colors.accent,
                )
            }
            Spacer(Modifier.width(12.dp))
            AddPill(onClick = onAdd)
        }
    }
}

@Composable
private fun AddPill(onClick: () -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .clip(PosTheme.shapes.pill)
            .background(colors.accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("+", style = PosTheme.typography.title3, color = colors.onAccent)
        Spacer(Modifier.width(6.dp))
        Text("Add", style = PosTheme.typography.headline, color = colors.onAccent)
    }
}

@Composable
private fun DotIndicator(count: Int, current: Int) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == current
            val color by animateColorAsState(
                targetValue = if (active) colors.accent else colors.separatorStrong,
                animationSpec = PosMotion.tweenStandard(),
                label = "dot",
            )
            val width by animateColorAsState(
                targetValue = color,
                animationSpec = PosMotion.tweenStandard(),
                label = "dot-w",
            )
            @Suppress("UNUSED_VARIABLE") val unused = width
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                    .clip(PosTheme.shapes.pill)
                    .background(color),
            )
        }
    }
}
