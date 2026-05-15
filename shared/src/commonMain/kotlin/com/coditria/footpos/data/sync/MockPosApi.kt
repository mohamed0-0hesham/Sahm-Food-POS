package com.coditria.footpos.data.sync

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.network.PosApi
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Stand-in for a real backend. Simulates network latency and an occasional failure
 * so the sync queue's retry behavior is observable end-to-end.
 *
 * Idempotency: tracks seen order IDs in memory and short-circuits duplicates.
 */
class MockPosApi(
    private val failureRate: Float = 0.20f,
    private val latencyMs: LongRange = 300L..1200L,
) : PosApi {

    private val seenOrderIds = mutableSetOf<String>()

    override suspend fun syncOrder(order: Order): Result<Unit> {
        delay(Random.nextLong(latencyMs.first, latencyMs.last).milliseconds)
        if (order.id.value in seenOrderIds) return Result.Success(Unit)
        if (Random.nextFloat() < failureRate) {
            return Result.Failure(AppError.NetworkError("Simulated transient failure"))
        }
        seenOrderIds += order.id.value
        return Result.Success(Unit)
    }
}
