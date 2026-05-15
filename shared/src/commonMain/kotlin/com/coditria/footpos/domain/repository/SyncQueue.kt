package com.coditria.footpos.domain.repository

import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncOperationState
import kotlinx.coroutines.flow.Flow

interface SyncQueue {
    fun observePending(): Flow<List<SyncOperation>>
    fun observeAll(): Flow<List<SyncOperation>>
    fun observeCountByState(state: SyncOperationState): Flow<Long>
    suspend fun enqueue(operation: SyncOperation)
    suspend fun markSynced(operationId: String)
    suspend fun markFailed(operationId: String, error: String)
    suspend fun incrementRetry(operationId: String, error: String)
    suspend fun retry(operationId: String)
}
