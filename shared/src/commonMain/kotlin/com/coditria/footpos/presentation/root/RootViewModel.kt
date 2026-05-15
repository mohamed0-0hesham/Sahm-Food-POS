package com.coditria.footpos.presentation.root

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.data.seed.ProductSeeder
import com.coditria.footpos.data.sync.SyncWorker
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.usecase.ObservePendingSyncCountUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RootState(
    val seeding: Boolean = true,
    val online: Boolean = true,
    val pendingSyncCount: Long = 0,
)

class RootViewModel(
    private val seeder: ProductSeeder,
    private val syncWorker: SyncWorker,
    networkMonitor: NetworkMonitor,
    observePendingCount: ObservePendingSyncCountUseCase,
) : MviViewModel<RootState, Nothing>() {

    override fun initialState() = RootState()

    init {
        viewModelScope.launch {
            seeder.seedIfEmpty()
            updateState { it.copy(seeding = false) }
        }
        syncWorker.start(viewModelScope)
        combine(networkMonitor.isOnline, observePendingCount()) { online, pending -> online to pending }
            .onEach { (online, pending) -> updateState { it.copy(online = online, pendingSyncCount = pending) } }
            .launchIn(viewModelScope)
    }
}
