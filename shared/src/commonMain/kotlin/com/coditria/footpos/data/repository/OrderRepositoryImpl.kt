package com.coditria.footpos.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.core.common.Uuid
import com.coditria.footpos.data.mapper.toDomain
import com.coditria.footpos.database.OrderEntity
import com.coditria.footpos.database.PosDatabase
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncStatus
import com.coditria.footpos.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class OrderRepositoryImpl(
    private val database: PosDatabase,
    private val dispatchers: DispatcherProvider,
) : OrderRepository {

    private val orderQueries get() = database.ordersQueries

    override fun observeAll(): Flow<List<Order>> =
        orderQueries.selectAllOrders().asFlow().mapToList(dispatchers.io).map { rows ->
            rows.map { it.toDomainWithItems() }
        }

    override suspend fun getById(id: OrderId): Order? = withContext(dispatchers.io) {
        orderQueries.selectOrderById(id.value).executeAsOneOrNull()?.toDomainWithItems()
    }

    override suspend fun save(order: Order): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            database.transaction {
                orderQueries.upsertOrder(
                    id = order.id.value,
                    status = order.status.name,
                    subtotalCents = order.subtotal.amountInCents,
                    taxCents = order.taxAmount.amountInCents,
                    discountCents = order.discount.amount.amountInCents,
                    totalCents = order.total.amountInCents,
                    taxRate = order.taxRate.value,
                    discountReason = order.discount.reason,
                    note = order.note,
                    paymentMethod = order.payment?.method?.name,
                    paymentAmountCents = order.payment?.amountTendered?.amountInCents,
                    syncStatus = order.syncStatus.name,
                    createdAt = order.createdAt.toEpochMilliseconds(),
                    updatedAt = order.updatedAt.toEpochMilliseconds(),
                )
                orderQueries.deleteItemsForOrder(order.id.value)
                order.items.forEach { item ->
                    orderQueries.upsertOrderItem(
                        id = Uuid.random(),
                        orderId = order.id.value,
                        productId = item.product.id.value,
                        productName = item.product.name,
                        quantity = item.quantity.toLong(),
                        unitPriceCents = item.unitPrice.amountInCents,
                        currency = item.unitPrice.currency.name,
                    )
                }
            }
        }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Failure(AppError.DatabaseError(it.message ?: "Failed to save order")) },
    )

    override suspend fun delete(id: OrderId): Result<Unit> = runCatching {
        withContext(dispatchers.io) { orderQueries.deleteOrder(id.value) }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Failure(AppError.DatabaseError(it.message ?: "Failed to delete order")) },
    )

    override suspend fun updateSyncStatus(id: OrderId, status: SyncStatus): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            orderQueries.updateSyncStatus(status.name, Clock.System.now().toEpochMilliseconds(), id.value)
        }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Failure(AppError.DatabaseError(it.message ?: "Failed to update sync status")) },
    )

    private fun OrderEntity.toDomainWithItems(): Order {
        val items = orderQueries.selectItemsForOrder(id).executeAsList().map { it.toDomain() }
        return toDomain(items)
    }
}
