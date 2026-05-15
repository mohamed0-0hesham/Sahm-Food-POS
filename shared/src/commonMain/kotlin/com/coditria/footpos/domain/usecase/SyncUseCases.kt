package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncOperationState
import com.coditria.footpos.domain.repository.SyncQueue
import kotlinx.coroutines.flow.Flow

class ObservePendingSyncUseCase(private val queue: SyncQueue) {
    operator fun invoke(): Flow<List<SyncOperation>> = queue.observePending()
}

class ObserveAllSyncUseCase(private val queue: SyncQueue) {
    operator fun invoke(): Flow<List<SyncOperation>> = queue.observeAll()
}

class ObservePendingSyncCountUseCase(private val queue: SyncQueue) {
    operator fun invoke(): Flow<Long> = queue.observeCountByState(SyncOperationState.PENDING)
}

class RetrySyncOperationUseCase(private val queue: SyncQueue) {
    suspend operator fun invoke(operationId: String) = queue.retry(operationId)
}
