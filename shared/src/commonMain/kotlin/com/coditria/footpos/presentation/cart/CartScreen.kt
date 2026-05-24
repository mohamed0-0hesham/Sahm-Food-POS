package com.coditria.footpos.presentation.cart

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.IconChip
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.ProductThumbnail
import com.coditria.footpos.core.designsystem.components.QuantityStepper
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderItem

@Composable
fun CartSidePanel(viewModel: CartViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CartContent(
        order = state.order,
        showHeader = true,
        onIncrement = viewModel::onIncrement,
        onDecrement = viewModel::onDecrement,
        onApplyDiscount = viewModel::onOpenDiscount,
        onCheckout = viewModel::onCheckout,
    )
}

@Composable
fun CartScreen(
    onBack: () -> Unit,
    viewModel: CartViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        CartNavBar(onBack = onBack, onClear = viewModel::onClearCart, count = state.order.items.size)
        Box(Modifier.weight(1f)) {
            CartContent(
                order = state.order,
                showHeader = false,
                onIncrement = viewModel::onIncrement,
                onDecrement = viewModel::onDecrement,
                onApplyDiscount = viewModel::onOpenDiscount,
                onCheckout = viewModel::onCheckout,
            )
        }
    }
}

@Composable
private fun CartNavBar(onBack: () -> Unit, onClear: () -> Unit, count: Int) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(glyph = "‹", onClick = onBack)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text("Your Bag", style = PosTheme.typography.title3, color = colors.labelPrimary)
            if (count > 0) {
                Text("$count items", style = PosTheme.typography.subhead, color = colors.labelSecondary)
            }
        }
        if (count > 0) {
            TertiaryButton(text = "Clear", onClick = onClear, destructive = true)
        }
    }
}

@Composable
private fun CartContent(
    order: Order,
    showHeader: Boolean,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onApplyDiscount: () -> Unit,
    onCheckout: () -> Unit,
) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxSize()) {
        if (showHeader) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Your Bag", style = PosTheme.typography.title2, color = colors.labelPrimary)
                if (order.items.isNotEmpty()) {
                    Text(
                        "${order.items.sumOf { it.quantity }} items",
                        style = PosTheme.typography.subhead,
                        color = colors.labelSecondary,
                    )
                }
            }
        }
        if (order.items.isEmpty()) {
            EmptyState(
                glyph = "▢",
                title = "Your bag is empty",
                message = "Tap a product in the catalog to start a new order.",
                modifier = Modifier.weight(1f),
            )
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(order.items, key = { it.product.id.value }) { item ->
                CartLineRow(item = item, onIncrement = onIncrement, onDecrement = onDecrement)
            }
        }
        CartTotalsBlock(order = order, onApplyDiscount = onApplyDiscount, onCheckout = onCheckout)
    }
}

@Composable
private fun CartLineRow(
    item: OrderItem,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.backgroundSecondary)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tiny thumbnail keeps the row visually anchored — cashiers can scan the bag
        // by image faster than by text alone.
        ProductThumbnail(
            product = item.product,
            modifier = Modifier
                .size(56.dp)
                .aspectRatio(1f)
                .clip(PosTheme.shapes.md),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.product.name,
                style = PosTheme.typography.headline,
                color = colors.labelPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${item.unitPrice.format()} · ${item.subtotal.format()}",
                style = PosTheme.typography.subhead,
                color = colors.labelSecondary,
            )
        }
        QuantityStepper(
            value = item.quantity,
            onIncrement = { onIncrement(item.product.id.value) },
            onDecrement = { onDecrement(item.product.id.value) },
        )
    }
}

@Composable
private fun CartTotalsBlock(order: Order, onApplyDiscount: () -> Unit, onCheckout: () -> Unit) {
    val colors = PosTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        TertiaryButton(
            text = "+ Apply discount",
            onClick = onApplyDiscount,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.lg)
                .background(colors.backgroundSecondary)
                .padding(16.dp),
        ) {
            TotalsRow("Subtotal", order.subtotal.formatAmount())
            Spacer(Modifier.height(6.dp))
            TotalsRow("Tax (${(order.taxRate.value * 100).toInt()}%)", order.taxAmount.formatAmount())
            if (!order.discount.amount.isZero()) {
                Spacer(Modifier.height(6.dp))
                TotalsRow(
                    "Discount",
                    "-" + order.discount.amount.formatAmount(),
                    valueColor = colors.success,
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
                Text(order.total.format(), style = PosTheme.typography.title2, color = colors.labelPrimary)
            }
        }
        Spacer(Modifier.size(16.dp))
        PrimaryButton(text = "Proceed to checkout", onClick = onCheckout, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TotalsRow(
    label: String,
    value: String,
    valueColor: Color? = null,
) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = valueColor ?: colors.labelPrimary)
    }
}
