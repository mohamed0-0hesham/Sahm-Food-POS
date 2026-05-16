package com.coditria.footpos.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.QuantityStepper
import com.coditria.footpos.core.designsystem.components.TertiaryButton
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
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("‹", style = PosTheme.typography.title2, color = colors.accent,
            modifier = Modifier.clickable { onBack() }.padding(end = 8.dp))
        Text("Current Order", style = PosTheme.typography.headline, color = colors.labelPrimary, modifier = Modifier.weight(1f))
        if (count > 0) {
            Text("Clear", style = PosTheme.typography.body, color = colors.destructive,
                modifier = Modifier.clickable { onClear() })
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
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Current Order", style = PosTheme.typography.title2, color = colors.labelPrimary, modifier = Modifier.weight(1f))
                if (order.items.isNotEmpty()) {
                    Box(
                        Modifier.clip(RoundedCornerShape(100)).background(colors.backgroundTertiary).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("${order.items.sumOf { it.quantity }}", style = PosTheme.typography.subhead, color = colors.labelSecondary)
                    }
                }
            }
        }
        if (order.items.isEmpty()) {
            EmptyState(
                glyph = "🛒",
                title = "Cart is empty",
                message = "Tap a product to add it",
            )
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.product.name, style = PosTheme.typography.headline, color = colors.labelPrimary)
            Spacer(Modifier.height(2.dp))
            Text(item.unitPrice.format(), style = PosTheme.typography.subhead, color = colors.labelSecondary)
        }
        QuantityStepper(
            value = item.quantity,
            onIncrement = { onIncrement(item.product.id.value) },
            onDecrement = { onDecrement(item.product.id.value) },
        )
        Spacer(Modifier.size(12.dp))
        Text(item.subtotal.formatAmount(), style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
}

@Composable
private fun CartTotalsBlock(order: Order, onApplyDiscount: () -> Unit, onCheckout: () -> Unit) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        TertiaryButton(text = "Apply discount", onClick = onApplyDiscount, modifier = Modifier.padding(bottom = 8.dp))
        TotalsRow(label = "Subtotal", value = order.subtotal.formatAmount(), bold = false)
        TotalsRow(label = "Tax (${(order.taxRate.value * 100).toInt()}%)", value = order.taxAmount.formatAmount(), bold = false)
        if (!order.discount.amount.isZero()) {
            TotalsRow(label = "Discount", value = "-" + order.discount.amount.formatAmount(), bold = false, valueColor = colors.success)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator).padding(vertical = 8.dp))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = PosTheme.typography.title2, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text(order.total.format(), style = PosTheme.typography.title2, color = colors.labelPrimary)
        }
        Spacer(Modifier.size(16.dp))
        PrimaryButton(text = "Checkout", onClick = onCheckout, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TotalsRow(
    label: String,
    value: String,
    bold: Boolean,
    valueColor: androidx.compose.ui.graphics.Color? = null,
) {
    val colors = PosTheme.colors
    val style = if (bold) PosTheme.typography.headline else PosTheme.typography.body
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = style, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = style, color = valueColor ?: colors.labelPrimary)
    }
}
