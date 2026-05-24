package com.coditria.footpos.presentation.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
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
import com.coditria.footpos.core.designsystem.components.PosCard
import com.coditria.footpos.core.designsystem.components.PosTopBar
import com.coditria.footpos.core.designsystem.components.SecondaryButton
import com.coditria.footpos.core.designsystem.components.SectionLabel
import com.coditria.footpos.core.designsystem.components.SyncStatusIcon
import com.coditria.footpos.core.designsystem.shapes
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
        PosTopBar(title = "Order #${orderId.value.takeLast(6)}", onBack = onBack)
        val order = state.order
        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (state.loading) "Loading…" else "Order not found",
                    style = PosTheme.typography.body,
                    color = colors.labelSecondary,
                )
            }
            return@Column
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(horizontal = 20.dp, vertical = 12.dp)),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Group(label = "Status") { StatusCard(order) }
            Group(label = "Items") { ItemsCard(order) }
            Group(label = "Totals") { TotalsCard(order) }
            order.payment?.let {
                Group(label = "Payment") { PaymentCard(order) }
            }
            error?.let {
                Text(it, style = PosTheme.typography.footnote, color = colors.destructive)
            }
            SecondaryButton(text = "Reprint receipt", onClick = vm::onReprint, modifier = Modifier.fillMaxWidth())
            if (order.syncStatus == SyncStatus.FAILED) {
                SecondaryButton(text = "Retry sync", onClick = vm::onRetrySync, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}

@Composable
private fun Group(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(text = label)
        content()
    }
}

@Composable
private fun StatusCard(order: Order) {
    val colors = PosTheme.colors
    PosCard {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SyncStatusIcon(order.syncStatus)
            Spacer(Modifier.size(10.dp))
            Text(
                order.syncStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                style = PosTheme.typography.headline,
                color = colors.labelPrimary,
            )
        }
    }
}

@Composable
private fun ItemsCard(order: Order) {
    val colors = PosTheme.colors
    PosCard {
        order.items.forEachIndexed { idx, item ->
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(PosTheme.shapes.sm)
                        .background(colors.backgroundSecondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "×${item.quantity}",
                        style = PosTheme.typography.subhead,
                        color = colors.labelPrimary,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.product.name, style = PosTheme.typography.headline, color = colors.labelPrimary)
                    Text(
                        "${item.unitPrice.format()} each",
                        style = PosTheme.typography.footnote,
                        color = colors.labelSecondary,
                    )
                }
                Text(item.subtotal.formatAmount(), style = PosTheme.typography.headline, color = colors.labelPrimary)
            }
            if (idx != order.items.lastIndex) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(colors.separator))
            }
        }
    }
}

@Composable
private fun TotalsCard(order: Order) {
    val colors = PosTheme.colors
    PosCard {
        Column(Modifier.padding(16.dp)) {
            Line("Subtotal", order.subtotal.format())
            Spacer(Modifier.size(4.dp))
            Line("Tax", order.taxAmount.format())
            if (!order.discount.amount.isZero()) {
                Spacer(Modifier.size(4.dp))
                Line("Discount", "-" + order.discount.amount.formatAmount(), valueColor = colors.success)
            }
            Spacer(Modifier.size(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
                Text(order.total.format(), style = PosTheme.typography.title2, color = colors.accent)
            }
        }
    }
}

@Composable
private fun PaymentCard(order: Order) {
    val payment = order.payment ?: return
    PosCard {
        Column(Modifier.padding(16.dp)) {
            Line("Method", payment.method.name.lowercase().replaceFirstChar { it.uppercase() })
            Spacer(Modifier.size(4.dp))
            Line("Received", payment.amountTendered.format())
            Spacer(Modifier.size(4.dp))
            Line("Change", payment.changeDue(order.total).format())
        }
    }
}

@Composable
private fun Line(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color? = null) {
    val colors = PosTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
        Text(value, style = PosTheme.typography.body, color = valueColor ?: colors.labelPrimary)
    }
}
