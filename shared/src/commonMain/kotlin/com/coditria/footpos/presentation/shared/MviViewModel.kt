package com.coditria.footpos.presentation.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lightweight MVI base. Concrete ViewModels expose `state` (immutable snapshot)
 * and `effects` (transient events: navigation, toasts, hardware results).
 * `updateState` and `emitEffect` are protected helpers — Compose only reads `state`.
 */
abstract class MviViewModel<State, Effect> : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    protected abstract fun initialState(): State

    protected val currentState: State get() = _state.value

    protected fun updateState(reducer: (State) -> State) = _state.update(reducer)

    protected fun emitEffect(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
