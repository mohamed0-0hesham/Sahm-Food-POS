package com.coditria.footpos.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.CategoryPill
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.SearchField
import com.coditria.footpos.core.designsystem.components.SectionLabel
import com.coditria.footpos.core.designsystem.components.SyncStatusIcon
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrderHistoryScreen(
    onOpenOrder: (OrderId) -> Unit,
    vm: OrderHistoryViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Column(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
            Text("Orders", style = PosTheme.typography.largeTitle, color = colors.labelPrimary)
            Text(
                "${state.orders.size} total",
                style = PosTheme.typography.subhead,
                color = colors.labelSecondary,
            )
            Spacer(Modifier.size(14.dp))
            SearchField(value = state.query, onValueChange = vm::onQueryChanged, placeholder = "Search orders")
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HistoryFilter.entries.forEach { f ->
                CategoryPill(label = f.label(), selected = f == state.filter, onClick = { vm.onFilterChanged(f) })
            }
        }
        Spacer(Modifier.size(12.dp))
        if (state.orders.isEmpty() && !state.loading) {
            EmptyState(
                glyph = "▤",
                title = "No orders yet",
                message = "Completed orders will appear here as you ring them up.",
                modifier = Modifier.weight(1f),
            )
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            state.grouped.forEach { (header, orders) ->
                item(key = header) {
                    SectionLabel(text = header, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }
                items(orders, key = { it.id.value }) { order ->
                    OrderHistoryRow(order = order, onClick = { onOpenOrder(order.id) })
                }
            }
        }
    }
}

private fun HistoryFilter.label(): String = when (this) {
    HistoryFilter.All -> "All"
    HistoryFilter.Synced -> "Synced"
    HistoryFilter.Pending -> "Pending"
    HistoryFilter.Failed -> "Failed"
}

@Composable
private fun OrderHistoryRow(order: Order, onClick: () -> Unit) {
    val colors = PosTheme.colors
    val time = order.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeText = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    val firstItems = order.items.take(3).joinToString(", ") { it.product.name }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.surfaceElevated)
            .border(width = 1.dp, color = colors.separator, shape = PosTheme.shapes.lg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Time tile — visually anchors the row and gives the eye a hierarchy cue.
        Box(
            Modifier
                .size(48.dp)
                .clip(PosTheme.shapes.md)
                .background(colors.backgroundSecondary),
            contentAlignment = Alignment.Center,
        ) {
            Text(timeText, style = PosTheme.typography.subhead, color = colors.labelPrimary)
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Order #${order.id.value.takeLast(6)}",
                    style = PosTheme.typography.headline,
                    color = colors.labelPrimary,
                )
                Spacer(Modifier.size(8.dp))
                SyncStatusIcon(order.syncStatus)
            }
            Spacer(Modifier.size(2.dp))
            Text(
                "${order.items.sumOf { it.quantity }} items · ${firstItems.ifBlank { "—" }}",
                style = PosTheme.typography.subhead,
                color = colors.labelSecondary,
                maxLines = 1,
            )
        }
        Spacer(Modifier.size(8.dp))
        Text(order.total.format(), style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}
