package com.coditria.footpos.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NavState(
    val activeTab: TabKey = TabKey.Sell,
    val stacks: Map<TabKey, List<Destination.Stacked>> = TabKey.entries.associateWith { emptyList() },
    val modal: Destination.Modal? = null,
)

/**
 * Minimal in-memory router. Three tabs, each with its own back stack, plus a single
 * modal slot. Keeping this hand-rolled (no Voyager) so the navigation model is
 * inspectable and trivially testable.
 */
class Navigator {
    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()

    fun selectTab(tab: TabKey) {
        _state.update { it.copy(activeTab = tab) }
    }

    fun push(destination: Destination.Stacked) {
        _state.update {
            val current = it.stacks[it.activeTab].orEmpty()
            it.copy(stacks = it.stacks + (it.activeTab to current + destination))
        }
    }

    /** Returns false if the stack was already empty (caller may want to handle system back). */
    fun pop(): Boolean {
        var popped = false
        _state.update {
            val current = it.stacks[it.activeTab].orEmpty()
            if (current.isEmpty()) it
            else {
                popped = true
                it.copy(stacks = it.stacks + (it.activeTab to current.dropLast(1)))
            }
        }
        return popped
    }

    fun popToRoot() {
        _state.update { it.copy(stacks = it.stacks + (it.activeTab to emptyList())) }
    }

    fun showModal(modal: Destination.Modal) {
        _state.update { it.copy(modal = modal) }
    }

    fun dismissModal() {
        _state.update { it.copy(modal = null) }
    }

    fun handleBack(): Boolean {
        if (_state.value.modal != null) {
            dismissModal()
            return true
        }
        return pop()
    }
}
