package com.coditria.footpos.presentation.orders

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
import com.coditria.footpos.core.designsystem.components.CategoryPill
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.SearchField
import com.coditria.footpos.core.designsystem.components.SyncStatusIcon
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
        Text("Orders", style = PosTheme.typography.largeTitle, color = colors.labelPrimary, modifier = Modifier.padding(20.dp))
        SearchField(value = state.query, onValueChange = vm::onQueryChanged, placeholder = "Search orders", modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.size(12.dp))
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryFilter.entries.forEach { f ->
                CategoryPill(label = f.label(), selected = f == state.filter, onClick = { vm.onFilterChanged(f) })
            }
        }
        Spacer(Modifier.size(12.dp))
        if (state.orders.isEmpty() && !state.loading) {
            EmptyState(glyph = "📋", title = "No orders yet", message = "Completed orders will appear here.")
            return@Column
        }
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.grouped.forEach { (header, orders) ->
                item(key = header) {
                    Text(header.uppercase(), style = PosTheme.typography.caption1, color = colors.labelTertiary, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
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
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundSecondary)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${order.id.value.takeLast(6)}", style = PosTheme.typography.headline, color = colors.labelPrimary)
                Spacer(Modifier.size(8.dp))
                SyncStatusIcon(order.syncStatus)
            }
            Text("$timeText · ${order.items.sumOf { it.quantity }} items", style = PosTheme.typography.footnote, color = colors.labelTertiary)
            if (firstItems.isNotEmpty()) {
                Text(firstItems, style = PosTheme.typography.body, color = colors.labelSecondary, maxLines = 1)
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(order.total.format(), style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}
