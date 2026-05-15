package com.coditria.footpos.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.database.PosDatabase
import com.coditria.footpos.database.SyncQueueEntity
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncOperationState
import com.coditria.footpos.domain.repository.SyncQueue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SyncQueueImpl(
    database: PosDatabase,
    private val dispatchers: DispatcherProvider,
) : SyncQueue {

    private val queries = database.syncQueueQueries
    private val json = Json { ignoreUnknownKeys = true }

    override fun observePending(): Flow<List<SyncOperation>> =
        queries.selectPending().asFlow().mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeAll(): Flow<List<SyncOperation>> =
        queries.selectAll().asFlow().mapToList(dispatchers.io)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeCountByState(state: SyncOperationState): Flow<Long> =
        queries.countByState(state.name).asFlow().mapToOne(dispatchers.io)

    override suspend fun enqueue(operation: SyncOperation) = withContext(dispatchers.io) {
        val (type, payload) = operation.serialize()
        queries.enqueue(
            id = operation.id,
            operationType = type,
            payload = payload,
            createdAt = operation.createdAt.toEpochMilliseconds(),
        )
    }

    override suspend fun markSynced(operationId: String) = withContext(dispatchers.io) {
        queries.markSynced(operationId)
    }

    override suspend fun markFailed(operationId: String, error: String) = withContext(dispatchers.io) {
        queries.markFailed(error, Clock.System.now().toEpochMilliseconds(), operationId)
    }

    override suspend fun incrementRetry(operationId: String, error: String) = withContext(dispatchers.io) {
        queries.incrementRetry(error, Clock.System.now().toEpochMilliseconds(), operationId)
    }

    override suspend fun retry(operationId: String) = withContext(dispatchers.io) {
        queries.resetToPending(operationId)
    }

    private fun SyncOperation.serialize(): Pair<String, String> = when (this) {
        is SyncOperation.CreateOrder -> "CREATE_ORDER" to json.encodeToString(
            CreateOrderPayload.serializer(),
            CreateOrderPayload(orderId.value),
        )
    }

    private fun SyncQueueEntity.toDomain(): SyncOperation = when (operationType) {
        "CREATE_ORDER" -> {
            val data = json.decodeFromString(CreateOrderPayload.serializer(), payload)
            SyncOperation.CreateOrder(
                id = id,
                orderId = OrderId(data.orderId),
                createdAt = Instant.fromEpochMilliseconds(createdAt),
                retryCount = retryCount.toInt(),
                lastAttemptAt = lastAttemptAt?.let { Instant.fromEpochMilliseconds(it) },
                state = SyncOperationState.valueOf(state),
                lastError = lastError,
            )
        }
        else -> error("Unknown sync operation type: $operationType")
    }

    @Serializable
    private data class CreateOrderPayload(val orderId: String)
}
