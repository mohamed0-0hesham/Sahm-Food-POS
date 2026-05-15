package com.coditria.footpos.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.SecondaryButton
import com.coditria.footpos.core.designsystem.components.SyncStatusIcon
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncStatus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OrderDetailScreen(
    orderId: OrderId,
    onBack: () -> Unit,
    onShowReceipt: (String) -> Unit,
    vm: OrderDetailViewModel = koinViewModel(parameters = { parametersOf(orderId) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vm) {
        vm.effects.collect { e ->
            when (e) {
                is OrderDetailEffect.Printed -> onShowReceipt(e.text)
                is OrderDetailEffect.PrintFailed -> error = e.message
                OrderDetailEffect.Cancelled -> onBack()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        TopBar(title = "Order #${orderId.value.takeLast(6)}", onBack = onBack)
        val order = state.order
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (state.loading) "Loading…" else "Order not found", style = PosTheme.typography.body, color = colors.labelSecondary)
            }
            return@Column
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SectionTitle("Status")
            StatusCard(order)
            SectionTitle("Items")
            ItemsCard(order)
            SectionTitle("Totals")
            TotalsCard(order)
            order.payment?.let {
                SectionTitle("Payment")
                PaymentCard(order)
            }
            error?.let {
                Text(it, style = PosTheme.typography.footnote, color = colors.destructive)
            }
            SecondaryButton(text = "Reprint Receipt", onClick = vm::onReprint, modifier = Modifier.fillMaxWidth())
            if (order.syncStatus == SyncStatus.FAILED) {
                SecondaryButton(text = "Retry Sync", onClick = vm::onRetrySync, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val colors = PosTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", style = PosTheme.typography.title2, color = colors.accent, modifier = Modifier.clickable { onBack() }.padding(end = 8.dp))
        Text(title, style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text.uppercase(), style = PosTheme.typography.caption1, color = PosTheme.colors.labelTertiary)
}

@Composable
private fun StatusCard(order: Order) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundSecondary).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SyncStatusIcon(order.syncStatus)
        Spacer(Modifier.size(8.dp))
        Text(order.syncStatus.name.lowercase().replaceFirstChar { it.uppercase() }, style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}

@Composable
private fun ItemsCard(order: Order) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundSecondary)) {
        order.items.forEachIndexed { idx, item ->
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${item.product.name} × ${item.quantity}", style = PosTheme.typography.headline, color = colors.labelPrimary)
                    Text("${item.unitPrice.format()} each", style = PosTheme.typography.footnote, color = colors.labelSecondary)
                }
                Text(item.subtotal.formatAmount(), style = PosTheme.typography.headline, color = colors.labelPrimary)
            }
            if (idx != order.items.lastIndex) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
            }
        }
    }
}

@Composable
private fun TotalsCard(order: Order) {
    val colors = PosTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundSecondary).padding(16.dp)) {
        Line("Subtotal", order.subtotal.format())
        Line("Tax", order.taxAmount.format())
        if (!order.discount.amount.isZero()) Line("Discount", "-" + order.discount.amount.formatAmount())
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator).padding(vertical = 6.dp))
        Spacer(Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text(order.total.format(), style = PosTheme.typography.title2, color = colors.labelPrimary)
        }
    }
}

@Composable
private fun PaymentCard(order: Order) {
    val payment = order.payment ?: return
    val colors = PosTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundSecondary).padding(16.dp)) {
        Line("Method", payment.method.name.lowercase().replaceFirstChar { it.uppercase() })
        Line("Received", payment.amountTendered.format())
        Line("Change", payment.changeDue(order.total).format())
    }
}

@Composable
private fun Line(label: String, value: String) {
    val colors = PosTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = colors.labelPrimary)
    }
}
