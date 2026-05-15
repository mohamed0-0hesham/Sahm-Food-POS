package com.coditria.footpos.domain.repository

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface OrderReader {
    fun observeAll(): Flow<List<Order>>
    suspend fun getById(id: OrderId): Order?
}

interface OrderWriter {
    suspend fun save(order: Order): Result<Unit>
    suspend fun delete(id: OrderId): Result<Unit>
    suspend fun updateSyncStatus(id: OrderId, status: SyncStatus): Result<Unit>
}

interface OrderRepository : OrderReader, OrderWriter
