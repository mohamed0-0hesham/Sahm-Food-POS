package com.coditria.footpos.data.sync

import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncStatus
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.network.PosApi
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.SyncQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Background sync engine.
 *
 * Listens to (pendingOperations × isOnline). When online with work to do,
 * processes each pending operation. Successes are removed from the queue and
 * the order is marked SYNCED; failures use the retry policy's exponential
 * backoff until either it succeeds or the policy gives up (FAILED).
 */
class SyncWorker(
    private val syncQueue: SyncQueue,
    private val orderRepository: OrderRepository,
    private val api: PosApi,
    private val retryPolicy: RetryPolicy,
    private val networkMonitor: NetworkMonitor,
    private val logger: Logger,
) {
    fun start(scope: CoroutineScope): Job = scope.launch {
        combine(syncQueue.observePending(), networkMonitor.isOnline) { ops, online -> ops to online }
            .distinctUntilChanged()
            .collect { (ops, online) ->
                if (!online || ops.isEmpty()) return@collect
                ops.forEach { processOperation(it) }
            }
    }

    private suspend fun processOperation(op: SyncOperation) {
        when (op) {
            is SyncOperation.CreateOrder -> processCreateOrder(op)
        }
    }

    private suspend fun processCreateOrder(op: SyncOperation.CreateOrder) {
        val order = orderRepository.getById(op.orderId)
        if (order == null) {
            logger.warn("Sync op references missing order ${op.orderId.value}; dropping")
            syncQueue.markSynced(op.id)
            return
        }
        when (val result = api.syncOrder(order)) {
            is Result.Success -> {
                syncQueue.markSynced(op.id)
                orderRepository.updateSyncStatus(order.id, SyncStatus.SYNCED)
                logger.info("Synced order ${order.id.value}")
            }
            is Result.Failure -> handleFailure(op, order.id, result.error.message)
        }
    }

    private suspend fun handleFailure(op: SyncOperation, orderId: OrderId, error: String) {
        val nextDelay = retryPolicy.nextDelay(op.retryCount)
        if (nextDelay == null) {
            syncQueue.markFailed(op.id, error)
            orderRepository.updateSyncStatus(orderId, SyncStatus.FAILED)
            logger.warn("Sync gave up for ${orderId.value}: $error")
        } else {
            logger.debug("Sync retry ${op.retryCount + 1} for ${orderId.value} after $nextDelay: $error")
            delay(nextDelay)
            syncQueue.incrementRetry(op.id, error)
        }
    }
}
