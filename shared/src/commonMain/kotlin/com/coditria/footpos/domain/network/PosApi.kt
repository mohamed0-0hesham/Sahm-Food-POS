package com.coditria.footpos.domain.network

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Order

/**
 * Remote API for syncing orders. Implementations are responsible for idempotency:
 * sending the same order twice (due to a retry) must not create a duplicate.
 */
interface PosApi {
    suspend fun syncOrder(order: Order): Result<Unit>
}
