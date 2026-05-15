package com.coditria.footpos.presentation.orders

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.usecase.ObserveOrderHistoryUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class HistoryFilter { All, Synced, Pending, Failed }

data class OrderHistoryState(
    val orders: List<Order> = emptyList(),
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter.All,
    val loading: Boolean = true,
) {
    val grouped: List<Pair<String, List<Order>>>
        get() {
            val zone = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(zone).date
            val yesterday = Clock.System.now().minus(1, DateTimeUnit.DAY, zone).toLocalDateTime(zone).date
            val weekAgo = Clock.System.now().minus(7, DateTimeUnit.DAY, zone).toLocalDateTime(zone).date

            val filtered = orders
                .asSequence()
                .filter { order ->
                    when (filter) {
                        HistoryFilter.All -> true
                        HistoryFilter.Synced -> order.syncStatus.name == "SYNCED"
                        HistoryFilter.Pending -> order.syncStatus.name == "PENDING"
                        HistoryFilter.Failed -> order.syncStatus.name == "FAILED"
                    }
                }
                .filter { order ->
                    val q = query.trim()
                    q.isEmpty() ||
                        order.id.value.contains(q, ignoreCase = true) ||
                        order.items.any { it.product.name.contains(q, ignoreCase = true) }
                }
                .toList()

            val groups = linkedMapOf<String, MutableList<Order>>(
                "Today" to mutableListOf(),
                "Yesterday" to mutableListOf(),
                "Earlier this week" to mutableListOf(),
                "Older" to mutableListOf(),
            )
            filtered.forEach { order ->
                val day = order.createdAt.toLocalDateTime(zone).date
                val key = when {
                    day == now -> "Today"
                    day == yesterday -> "Yesterday"
                    day > weekAgo -> "Earlier this week"
                    else -> "Older"
                }
                groups.getValue(key) += order
            }
            return groups.filterValues { it.isNotEmpty() }.entries.map { (k, v) -> k to v.toList() }
        }
}

class OrderHistoryViewModel(
    observe: ObserveOrderHistoryUseCase,
) : MviViewModel<OrderHistoryState, Nothing>() {

    override fun initialState() = OrderHistoryState()

    init {
        observe()
            .onEach { list -> updateState { it.copy(orders = list, loading = false) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(q: String) = updateState { it.copy(query = q) }

    fun onFilterChanged(f: HistoryFilter) = updateState { it.copy(filter = f) }
}
