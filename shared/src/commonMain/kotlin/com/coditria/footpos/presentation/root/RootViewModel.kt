package com.coditria.footpos.presentation.root

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.data.seed.ProductSeeder
import com.coditria.footpos.data.sync.SyncWorker
import com.coditria.footpos.domain.model.User
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.usecase.ObserveCurrentUserUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncCountUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RootState(
    val seeding: Boolean = true,
    val authResolved: Boolean = false,
    val currentUser: User? = null,
    val online: Boolean = true,
    val pendingSyncCount: Long = 0,
) {
    val isAuthenticated: Boolean get() = currentUser != null
}

class RootViewModel(
    private val seeder: ProductSeeder,
    private val syncWorker: SyncWorker,
    networkMonitor: NetworkMonitor,
    observePendingCount: ObservePendingSyncCountUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
) : MviViewModel<RootState, Nothing>() {

    override fun initialState() = RootState()

    init {
        viewModelScope.launch {
            seeder.seedIfEmpty()
            updateState { it.copy(seeding = false) }
        }
        syncWorker.start(viewModelScope)

        observeCurrentUser()
            .onEach { user ->
                updateState { it.copy(currentUser = user, authResolved = true) }
            }
            .launchIn(viewModelScope)

        networkMonitor.isOnline
            .onEach { online -> updateState { it.copy(online = online) } }
            .launchIn(viewModelScope)

        observePendingCount()
            .onEach { count -> updateState { it.copy(pendingSyncCount = count) } }
            .launchIn(viewModelScope)
    }
}
