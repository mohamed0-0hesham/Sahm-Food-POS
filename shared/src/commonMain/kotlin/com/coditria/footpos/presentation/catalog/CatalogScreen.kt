package com.coditria.footpos.presentation.catalog

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.CategoryPill
import com.coditria.footpos.core.designsystem.components.SearchField
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
            CatalogTopBar(
                query = state.searchQuery,
                onQueryChange = catalogVm::onSearchChanged,
                onOpenSync = onOpenSyncStatus,
            )
            CategoryStrip(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = catalogVm::onCategorySelected,
            )
            // Map product id → quantity in cart, so each card can show a badge.
            val cartQuantities = state.cart.items.associate { it.product.id.value to it.quantity }
            ProductGrid(
                products = state.visibleProducts,
                cartQuantities = cartQuantities,
                isTablet = isTablet,
                onTap = catalogVm::onProductTapped,
                onLongPress = catalogVm::onProductLongPressed,
                modifier = Modifier.weight(1f),
            )
            if (!isTablet && state.cart.items.isNotEmpty()) {
                FloatingCartBar(
                    itemCount = state.cart.items.sumOf { it.quantity },
                    total = state.cart.total.format(),
                    onClick = onOpenCartPhone,
                )
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
private fun CatalogTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSync: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search products",
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(12.dp))
        SyncIconButton(onClick = onOpenSync)
    }
}

@Composable
private fun SyncIconButton(onClick: () -> Unit) {
    val colors = PosTheme.colors
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(colors.backgroundSecondary).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("☁", style = PosTheme.typography.title3, color = colors.labelPrimary)
    }
}

@Composable
private fun CategoryStrip(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(categories) { cat ->
            CategoryPill(label = cat, selected = cat == selected, onClick = { onSelect(cat) })
        }
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
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    // Wrap the card in an outer Box so the quantity badge can float over the top-right corner.
    Box {
        Column(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.backgroundTertiary)
                .clickable(onClick = onTap),
        ) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(1f).background(colors.backgroundSecondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(product.emoji ?: product.name.take(1).uppercase(), style = PosTheme.typography.largeTitle)
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    style = PosTheme.typography.headline,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(product.price.format(), style = PosTheme.typography.subhead, color = colors.labelSecondary)
            }
        }
        if (quantityInCart > 0) {
            QuantityBadge(
                count = quantityInCart,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
    }
    // Long-press handler is wired upstream — kept in the signature for the upcoming
    // combinedClickable migration so callers don't need to change later.
    @Suppress("UNUSED_EXPRESSION") onLongPress
}

@Composable
private fun QuantityBadge(count: Int, modifier: Modifier = Modifier) {
    val colors = PosTheme.colors
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(colors.accent)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            count.toString(),
            style = PosTheme.typography.subhead,
            color = androidx.compose.ui.graphics.Color.White,
        )
    }
}

@Composable
private fun FloatingCartBar(itemCount: Int, total: String, onClick: () -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.accent)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("View Cart ($itemCount)", style = PosTheme.typography.headline, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.weight(1f))
        Text(total, style = PosTheme.typography.headline, color = androidx.compose.ui.graphics.Color.White)
    }
}
