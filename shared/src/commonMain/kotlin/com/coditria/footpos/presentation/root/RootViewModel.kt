package com.coditria.footpos.presentation.root

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.data.sync.SyncWorker
import com.coditria.footpos.domain.model.User
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.usecase.ObserveCurrentUserUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncCountUseCase
import com.coditria.footpos.domain.usecase.RefreshProductsUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RootState(
    val authResolved: Boolean = false,
    val currentUser: User? = null,
    val online: Boolean = true,
    val pendingSyncCount: Long = 0,
) {
    val isAuthenticated: Boolean get() = currentUser != null
}

class RootViewModel(
    private val refreshProducts: RefreshProductsUseCase,
    private val syncWorker: SyncWorker,
    networkMonitor: NetworkMonitor,
    observePendingCount: ObservePendingSyncCountUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
) : MviViewModel<RootState, Nothing>() {

    override fun initialState() = RootState()

    init {
        // Offline-first: never block the UI on the network. The catalog observes the
        // local cache (empty on first launch when offline, populated on later launches
        // and as soon as a refresh completes). The refresh runs fire-and-forget here.
        viewModelScope.launch { refreshProducts() }

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
