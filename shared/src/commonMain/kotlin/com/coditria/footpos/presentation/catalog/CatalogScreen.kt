package com.coditria.footpos.presentation.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import androidx.compose.foundation.shape.CircleShape
import com.coditria.footpos.core.designsystem.components.CategoryPill
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.ProductThumbnail
import com.coditria.footpos.core.designsystem.components.SearchField
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.presentation.cart.CartSidePanel
import com.coditria.footpos.presentation.cart.CartViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogScreen(
    isTablet: Boolean,
    onOpenSyncStatus: () -> Unit,
    onOpenCartPhone: () -> Unit,
    catalogVm: CatalogViewModel = koinViewModel(),
    cartVm: CartViewModel = koinViewModel(),
) {
    val state by catalogVm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Row(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        Column(Modifier.weight(1f)) {
            val cartQuantities = state.cart.items.associate { it.product.id.value to it.quantity }
            CatalogHeader(
                query = state.searchQuery,
                online = state.online,
                onQueryChange = catalogVm::onSearchChanged,
                onOpenSync = onOpenSyncStatus,
            )
            // Best Sellers ride above the category strip on the phone layout — the
            // tablet keeps the strip-then-grid composition since the side-panel
            // already provides a hero focal area on the right.
            if (!isTablet && state.bestSellers.isNotEmpty()) {
                Spacer(Modifier.size(8.dp))
                BestSellersCarousel(
                    sellers = state.bestSellers,
                    cartQuantities = cartQuantities,
                    onAdd = catalogVm::onProductTapped,
                    onLongPress = catalogVm::onProductLongPressed,
                )
                Spacer(Modifier.size(12.dp))
            }
            CategoryStrip(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = catalogVm::onCategorySelected,
            )
            Box(Modifier.weight(1f)) {
                if (state.visibleProducts.isEmpty()) {
                    CatalogEmptyState(
                        hasAnyProducts = state.products.isNotEmpty(),
                        hasQuery = state.searchQuery.isNotBlank(),
                    )
                } else {
                    ProductGrid(
                        products = state.visibleProducts,
                        cartQuantities = cartQuantities,
                        isTablet = isTablet,
                        onTap = catalogVm::onProductTapped,
                        onLongPress = catalogVm::onProductLongPressed,
                    )
                }
            }
            // Phone-only floating cart bar — animated entrance so adding the first
            // item feels like the cart "rises" into view rather than just appearing.
            if (!isTablet) {
                AnimatedVisibility(
                    visible = state.cart.items.isNotEmpty(),
                    enter = slideInVertically(PosMotion.tweenStandard()) { it } + fadeIn(PosMotion.tweenStandard()),
                    exit = slideOutVertically(PosMotion.tweenFast()) { it } + fadeOut(PosMotion.tweenFast()),
                ) {
                    FloatingCartBar(
                        itemCount = state.cart.items.sumOf { it.quantity },
                        total = state.cart.total.format(),
                        onClick = onOpenCartPhone,
                    )
                }
            }
        }
        if (isTablet) {
            Box(
                Modifier
                    .width(360.dp)
                    .fillMaxSize()
                    .background(colors.backgroundSecondary),
            ) {
                CartSidePanel(viewModel = cartVm)
            }
        }
    }
}

@Composable
private fun CatalogHeader(
    query: String,
    online: Boolean,
    onQueryChange: (String) -> Unit,
    onOpenSync: () -> Unit,
) {
    val colors = PosTheme.colors
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Discover",
                    style = PosTheme.typography.largeTitle,
                    color = colors.labelPrimary,
                )
                Text(
                    "Today's catalog",
                    style = PosTheme.typography.subhead,
                    color = colors.labelSecondary,
                )
            }
            OnlinePill(online = online, onClick = onOpenSync)
        }
        Spacer(Modifier.height(16.dp))
        SearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search products",
        )
    }
}

/**
 * Status pill that doubles as the sync-sheet entry point. Green dot when online,
 * amber when offline — gives cashiers an immediate read on connectivity (which is
 * the single biggest source of "is something broken?" confusion in POS).
 */
@Composable
private fun OnlinePill(online: Boolean, onClick: () -> Unit) {
    val colors = PosTheme.colors
    val dot = if (online) colors.success else colors.warning
    Row(
        Modifier
            .clip(PosTheme.shapes.pill)
            .background(colors.backgroundSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(8.dp).clip(CircleShape).background(dot),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            if (online) "Online" else "Offline",
            style = PosTheme.typography.subhead,
            color = colors.labelPrimary,
        )
    }
}

@Composable
private fun CategoryStrip(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories) { cat ->
            CategoryPill(label = cat, selected = cat == selected, onClick = { onSelect(cat) })
        }
    }
}

@Composable
private fun CatalogEmptyState(
    hasAnyProducts: Boolean,
    hasQuery: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        !hasAnyProducts -> EmptyState(
            glyph = "▢",
            title = "Catalog is empty",
            message = "We couldn't load any products. Check your connection — items will appear as soon as the device is online.",
            modifier = modifier,
        )
        hasQuery -> EmptyState(
            glyph = "⌕",
            title = "No matches",
            message = "Try a different search term or clear the filter.",
            modifier = modifier,
        )
        else -> EmptyState(
            glyph = "▦",
            title = "Nothing in this category",
            message = "Pick another category to see more products.",
            modifier = modifier,
        )
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    cartQuantities: Map<String, Int>,
    isTablet: Boolean,
    onTap: (Product) -> Unit,
    onLongPress: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isTablet) 4 else 2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(products, key = { it.id.value }) { product ->
            ProductCard(
                product = product,
                quantityInCart = cartQuantities[product.id.value] ?: 0,
                onTap = { onTap(product) },
                onLongPress = { onLongPress(product) },
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val colors = PosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = PosMotion.tweenFast(),
        label = "card-press",
    )
    Box(
        Modifier
            .scale(scale)
            .clip(PosTheme.shapes.lg)
            .background(colors.surfaceElevated)
            .border(width = 1.dp, color = colors.separator, shape = PosTheme.shapes.lg)
            .clickable(interactionSource = interaction, indication = null, onClick = onTap),
    ) {
        Column {
            Box {
                ProductThumbnail(
                    product = product,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(PosTheme.shapes.lg),
                )
                if (quantityInCart > 0) {
                    QuantityBadge(
                        count = quantityInCart,
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    )
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    product.name,
                    style = PosTheme.typography.headline,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                product.category?.let {
                    Text(
                        it,
                        style = PosTheme.typography.caption1,
                        color = colors.labelTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    product.price.format(),
                    style = PosTheme.typography.title3,
                    color = colors.accent,
                )
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") onLongPress
}

@Composable
private fun QuantityBadge(count: Int, modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    Box(
        modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(colors.labelPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            count.toString(),
            style = PosTheme.typography.subhead,
            color = colors.labelInverted,
        )
    }
}

/**
 * Phone-only floating cart pill. Sits above the tab bar with a clear CTA chevron
 * so the cashier always sees a path to checkout from anywhere in the catalog.
 */
@Composable
private fun FloatingCartBar(itemCount: Int, total: String, onClick: () -> Unit) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.lg)
                .background(colors.labelPrimary)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    itemCount.toString(),
                    style = PosTheme.typography.subhead,
                    color = colors.onAccent,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "View order",
                style = PosTheme.typography.headline,
                color = colors.labelInverted,
                modifier = Modifier.weight(1f),
            )
            Text(total, style = PosTheme.typography.headline, color = colors.labelInverted)
            Spacer(Modifier.width(6.dp))
            Text("›", style = PosTheme.typography.title3, color = colors.labelInverted)
        }
    }
}
