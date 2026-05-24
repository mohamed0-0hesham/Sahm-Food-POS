package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.now
import com.coditria.footpos.domain.model.HourlyBucket
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderStatus
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.TodayStats
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.ProductRepository
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Top-selling products across the full order history.
 *
 * Important: the `Product` snapshot stored on each [OrderItem] is intentionally
 * thin (id / name / price only — see `OrderItemEntity` schema), because orders are
 * a historical transaction record, not a denormalized copy of the catalog. To
 * render rich UI (images, categories, etc.) we **re-hydrate** each top-seller
 * against the live catalog and fall back to the order snapshot only when the
 * catalog no longer has the product.
 */
class ObserveTopSellersUseCase(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) {
    operator fun invoke(limit: Int = 5): Flow<List<Pair<Product, Int>>> = combine(
        orderRepository.observeAll(),
        productRepository.observeAll(),
    ) { orders, catalog ->
        aggregateTopSellers(orders, catalog, limit)
    }
}

private fun aggregateTopSellers(
    orders: List<Order>,
    catalog: List<Product>,
    limit: Int,
): List<Pair<Product, Int>> {
    val catalogById = catalog.associateBy { it.id.value }
    return orders
        .asSequence()
        .filter { it.status != OrderStatus.CANCELLED }
        .flatMap { it.items.asSequence() }
        .groupingBy { it.product.id.value }
        .fold(0 to (null as Product?)) { acc, item ->
            (acc.first + item.quantity) to item.product
        }
        .entries
        .mapNotNull { (id, qtyAndSnapshot) ->
            val (qty, snapshot) = qtyAndSnapshot
            // Prefer the live catalog product (has imageUrl, category, etc.);
            // fall back to the order-time snapshot if the catalog no longer has
            // this product.
            val product = catalogById[id] ?: snapshot ?: return@mapNotNull null
            product to qty
        }
        .sortedByDescending { it.second }
        .take(limit)
}

/**
 * Today-only revenue, order count, top item, hourly bars + peak hour.
 *
 * The "top item" gets the same catalog re-hydration as bestsellers above so the
 * dashboard tile can show a current product name even if the historical order
 * stored a stale one.
 */
class ObserveTodayStatsUseCase(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalTime::class)
    operator fun invoke(): Flow<TodayStats> = combine(
        orderRepository.observeAll(),
        productRepository.observeAll(),
        settingsRepository.observe(),
    ) { orders, catalog, settings ->
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

        // Top item — by units sold within today's orders. Re-hydrate against the
        // live catalog so the dashboard tile has the canonical product (this isn't
        // currently used to render an image, but keeps the contract consistent
        // with the bestsellers carousel).
        val catalogById = catalog.associateBy { it.id.value }
        val topPair = todayOrders
            .asSequence()
            .flatMap { it.items.asSequence() }
            .groupingBy { it.product.id.value }
            .fold(0 to (null as Product?)) { acc, item ->
                (acc.first + item.quantity) to item.product
            }
            .entries
            .mapNotNull { (id, qtyAndSnapshot) ->
                val (qty, snapshot) = qtyAndSnapshot
                val product = catalogById[id] ?: snapshot ?: return@mapNotNull null
                product to qty
            }
            .maxByOrNull { it.second }

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
            topItem = topPair?.first,
            topItemUnitsSold = topPair?.second ?: 0,
            hourlyRevenue = buckets,
            peakHour = buckets.maxByOrNull { it.revenue.amountInCents },
        )
    }
}
