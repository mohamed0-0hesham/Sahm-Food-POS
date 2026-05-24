package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.now
import com.coditria.footpos.domain.model.HourlyBucket
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderStatus
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.TodayStats
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Top-selling products across the full order history.
 *
 * Aggregation lives in the domain (not the data layer) because "what's a best seller"
 * is a business rule — today it's "most units sold across all paid orders", tomorrow
 * it could weigh recency, revenue, or category. The repository stays a dumb store.
 */
class ObserveTopSellersUseCase(
    private val orderRepository: OrderRepository,
) {
    operator fun invoke(limit: Int = 5): Flow<List<Pair<Product, Int>>> =
        orderRepository.observeAll().map { orders ->
            orders
                .asSequence()
                .filter { it.status != OrderStatus.CANCELLED }
                .flatMap { it.items.asSequence() }
                .groupingBy { it.product.id.value }
                .fold(0 to (null as Product?)) { acc, item ->
                    (acc.first + item.quantity) to item.product
                }
                .values
                .mapNotNull { (qty, product) -> product?.let { it to qty } }
                .sortedByDescending { it.second }
                .take(limit)
                .toList()
        }
}

/**
 * Today-only revenue, order count, top item, hourly bars + peak hour.
 *
 * Combined with the [SettingsRepository] currency so an empty-day [Money.ZERO] still
 * carries the store's configured currency rather than the hard-coded default.
 */
class ObserveTodayStatsUseCase(
    private val orderRepository: OrderRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalTime::class)
    operator fun invoke(): Flow<TodayStats> = combine(
        orderRepository.observeAll(),
        settingsRepository.observe(),
    ) { orders, settings ->
        val tz = TimeZone.currentSystemDefault()
        val today = now().toLocalDateTime(tz).date
        val currency = settings.currency

        val todayOrders = orders.filter {
            it.status != OrderStatus.CANCELLED &&
                it.createdAt.toLocalDateTime(tz).date == today
        }

        if (todayOrders.isEmpty()) {
            return@combine TodayStats(
                revenue = Money(0, currency),
                orderCount = 0,
                avgTicket = Money(0, currency),
                topItem = null,
                topItemUnitsSold = 0,
                hourlyRevenue = emptyList(),
                peakHour = null,
            )
        }

        val revenueCents = todayOrders.sumOf { it.total.amountInCents }
        val revenue = Money(revenueCents, currency)
        val avg = Money(revenueCents / todayOrders.size, currency)

        // Top item — by units sold within today's orders.
        val topPair = todayOrders
            .asSequence()
            .flatMap { it.items.asSequence() }
            .groupingBy { it.product.id.value }
            .fold(0 to (null as Product?)) { acc, item ->
                (acc.first + item.quantity) to item.product
            }
            .values
            .maxByOrNull { it.first }

        // Hourly buckets — group by local hour. Sparse: only hours with orders appear.
        val buckets = todayOrders
            .groupBy { it.createdAt.toLocalDateTime(tz).hour }
            .map { (hour, hourOrders) ->
                HourlyBucket(
                    hour = hour,
                    revenue = Money(hourOrders.sumOf { it.total.amountInCents }, currency),
                    orderCount = hourOrders.size,
                )
            }
            .sortedBy { it.hour }

        TodayStats(
            revenue = revenue,
            orderCount = todayOrders.size,
            avgTicket = avg,
            topItem = topPair?.second,
            topItemUnitsSold = topPair?.first ?: 0,
            hourlyRevenue = buckets,
            peakHour = buckets.maxByOrNull { it.revenue.amountInCents },
        )
    }
}
