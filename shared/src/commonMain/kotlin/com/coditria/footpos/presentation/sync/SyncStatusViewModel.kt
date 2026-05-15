package com.coditria.footpos.presentation.sync

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.usecase.ObserveAllSyncUseCase
import com.coditria.footpos.domain.usecase.RetrySyncOperationUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SyncStatusState(
    val operations: List<SyncOperation> = emptyList(),
    val online: Boolean = true,
)

class SyncStatusViewModel(
    observeAll: ObserveAllSyncUseCase,
    networkMonitor: NetworkMonitor,
    private val retry: RetrySyncOperationUseCase,
) : MviViewModel<SyncStatusState, Nothing>() {

    override fun initialState() = SyncStatusState()

    init {
        combine(observeAll(), networkMonitor.isOnline) { ops, online -> ops to online }
            .onEach { (ops, online) -> updateState { it.copy(operations = ops, online = online) } }
            .launchIn(viewModelScope)
    }

    fun onRetry(operationId: String) = launch { retry(operationId) }
}
