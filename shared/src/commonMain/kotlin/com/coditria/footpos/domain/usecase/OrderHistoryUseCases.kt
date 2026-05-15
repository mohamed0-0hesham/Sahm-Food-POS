package com.coditria.footpos.domain.usecase

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncStatus
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.SyncQueue
import kotlinx.coroutines.flow.Flow

class ObserveOrderHistoryUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<List<Order>> = repository.observeAll()
}

class GetOrderByIdUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: OrderId): Order? = repository.getById(id)
}

class CancelOrderUseCase(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(id: OrderId): Result<Unit> = repository.delete(id)
}

class RetryOrderSyncUseCase(
    private val orderRepository: OrderRepository,
    private val syncQueue: SyncQueue,
) {
    suspend operator fun invoke(orderId: OrderId): Result<Unit> {
        orderRepository.updateSyncStatus(orderId, SyncStatus.PENDING)
        return Result.Success(Unit)
    }
}
