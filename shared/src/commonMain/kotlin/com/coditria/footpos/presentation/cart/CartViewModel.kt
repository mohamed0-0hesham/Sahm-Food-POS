package com.coditria.footpos.presentation.cart

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.core.navigation.Destination
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.usecase.ApplyDiscountUseCase
import com.coditria.footpos.domain.usecase.ClearCartUseCase
import com.coditria.footpos.domain.usecase.ObserveCartUseCase
import com.coditria.footpos.domain.usecase.RemoveItemFromCartUseCase
import com.coditria.footpos.domain.usecase.SetOrderNoteUseCase
import com.coditria.footpos.domain.usecase.UpdateItemQuantityUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class CartState(val order: Order = Order.newDraft())

sealed interface CartEffect

class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val removeItem: RemoveItemFromCartUseCase,
    private val updateQuantity: UpdateItemQuantityUseCase,
    private val applyDiscount: ApplyDiscountUseCase,
    private val setNote: SetOrderNoteUseCase,
    private val clearCart: ClearCartUseCase,
    private val navigator: Navigator,
) : MviViewModel<CartState, CartEffect>() {

    override fun initialState() = CartState()

    init {
        observeCart()
            .onEach { order -> updateState { it.copy(order = order) } }
            .launchIn(viewModelScope)
    }

    fun onIncrement(productIdValue: String) = launch {
        val current = currentState.order.items.find { it.product.id.value == productIdValue }?.quantity ?: 0
        updateQuantity(productIdValue, current + 1)
    }

    fun onDecrement(productIdValue: String) = launch {
        val current = currentState.order.items.find { it.product.id.value == productIdValue }?.quantity ?: 0
        if (current <= 1) removeItem(productIdValue) else updateQuantity(productIdValue, current - 1)
    }

    fun onRemove(productIdValue: String) = launch { removeItem(productIdValue) }

    fun onApplyDiscount(amount: Money, reason: String?) = launch { applyDiscount(amount, reason) }

    fun onClearDiscount() = launch { applyDiscount(Money.ZERO) }

    fun onSetNote(note: String?) = launch { setNote(note) }

    fun onClearCart() = launch { clearCart() }

    fun onCheckout() = navigator.showModal(Destination.Modal.Checkout)

    fun onOpenDiscount() = navigator.showModal(Destination.Modal.Discount)
}
