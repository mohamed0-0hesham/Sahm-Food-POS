package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.now

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.core.common.Uuid
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderStatus
import com.coditria.footpos.domain.model.Payment
import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncStatus
import com.coditria.footpos.domain.repository.CartRepository
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.SyncQueue

/**
 * Snapshots the cart, persists it as a PAID order, enqueues a sync operation,
 * and clears the cart so the cashier is ready for the next order.
 *
 * The local DB is the source of truth — sync happens eventually in the background.
 */
class CompleteOrderUseCase(
    private val cart: CartRepository,
    private val orderRepository: OrderRepository,
    private val syncQueue: SyncQueue,
) {
    suspend operator fun invoke(payment: Payment): Result<Order> {
        val snapshot = cart.snapshot()
        if (snapshot.items.isEmpty()) {
            return Result.Failure(AppError.ValidationError("Cart is empty"))
        }
        val now = now()
        val completed = snapshot.copy(
            status = OrderStatus.PAID,
            payment = payment,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING,
        )
        return when (val saveResult = orderRepository.save(completed)) {
            is Result.Success -> {
                syncQueue.enqueue(
                    SyncOperation.CreateOrder(
                        id = Uuid.random(),
                        orderId = completed.id,
                        createdAt = now,
                    )
                )
                cart.clear()
                Result.Success(completed)
            }
            is Result.Failure -> saveResult
        }
    }
}
