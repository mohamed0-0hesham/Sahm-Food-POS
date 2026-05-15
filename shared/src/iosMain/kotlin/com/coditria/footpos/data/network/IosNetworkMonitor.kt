package com.coditria.footpos.data.network

import com.coditria.footpos.domain.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal iOS placeholder. A production app would wrap NWPathMonitor — out of scope
 * for this challenge per SAHM_POS_SPEC §2 (mock implementation acceptable).
 */
class IosNetworkMonitor : NetworkMonitor {
    private val state = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = state.asStateFlow()
}
