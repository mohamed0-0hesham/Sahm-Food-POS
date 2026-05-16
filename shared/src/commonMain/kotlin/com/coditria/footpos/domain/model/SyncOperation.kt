package com.coditria.footpos.domain.model

import kotlin.time.Instant

enum class SyncOperationState { PENDING, FAILED }

sealed interface SyncOperation {
    val id: String
    val createdAt: Instant
    val retryCount: Int
    val lastAttemptAt: Instant?
    val state: SyncOperationState
    val lastError: String?

    data class CreateOrder(
        override val id: String,
        val orderId: OrderId,
        override val createdAt: Instant,
        override val retryCount: Int = 0,
        override val lastAttemptAt: Instant? = null,
        override val state: SyncOperationState = SyncOperationState.PENDING,
        override val lastError: String? = null,
    ) : SyncOperation
}
