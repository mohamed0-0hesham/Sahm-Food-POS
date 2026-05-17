package com.coditria.footpos.presentation.checkout

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.core.navigation.Destination
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.Payment
import com.coditria.footpos.domain.model.PaymentMethod
import com.coditria.footpos.domain.usecase.CompleteOrderUseCase
import com.coditria.footpos.domain.usecase.GetSettingsUseCase
import com.coditria.footpos.domain.usecase.ObserveCartUseCase
import com.coditria.footpos.domain.usecase.PrintReceiptUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class CheckoutState(
    val cart: Order = Order.newDraft(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val amountTenderedCents: Long = 0L,
    val processing: Boolean = false,
    val error: String? = null,
) {
    val total: Money get() = cart.total
    val change: Money
        get() {
            val diff = amountTenderedCents - total.amountInCents
            return if (diff > 0) Money(diff, total.currency) else Money(0, total.currency)
        }
    val canComplete: Boolean
        get() = !processing && cart.items.isNotEmpty() &&
            (paymentMethod != PaymentMethod.CASH || amountTenderedCents >= total.amountInCents)
}

sealed interface CheckoutEffect {
    data class OrderCompleted(val order: Order, val printedReceipt: String?) : CheckoutEffect
    data class Failed(val message: String) : CheckoutEffect
}

class CheckoutViewModel(
    observeCart: ObserveCartUseCase,
    private val completeOrder: CompleteOrderUseCase,
    private val printReceipt: PrintReceiptUseCase,
    private val getSettings: GetSettingsUseCase,
    private val navigator: Navigator,
) : MviViewModel<CheckoutState, CheckoutEffect>() {

    override fun initialState() = CheckoutState()

    init {
        observeCart()
            .onEach { order ->
                updateState {
                    it.copy(
                        cart = order,
                        amountTenderedCents = it.amountTenderedCents.coerceAtLeast(0),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPaymentMethodChanged(method: PaymentMethod) =
        updateState { it.copy(paymentMethod = method) }

    fun onAmountChanged(cents: Long) =
        updateState { it.copy(amountTenderedCents = cents.coerceAtLeast(0)) }

    fun onCancel() = navigator.dismissModal()

    fun onComplete() = launch {
        val state = currentState
        if (!state.canComplete) return@launch
        updateState { it.copy(processing = true, error = null) }
        val tendered = if (state.paymentMethod == PaymentMethod.CASH) {
            Money(state.amountTenderedCents, state.total.currency)
        } else {
            state.total
        }
        val payment = Payment(state.paymentMethod, tendered)
        when (val res = completeOrder(payment)) {
            is Result.Success -> {
                val printed = if (getSettings().autoPrintReceipts) {
                    when (val print = printReceipt(res.value)) {
                        is Result.Success -> print.value
                        is Result.Failure -> null
                    }
                } else null
                updateState { it.copy(processing = false) }
                navigator.dismissModal()
                navigator.showModal(
                    Destination.Modal.Receipt(
                        orderId = res.value.id,
                        printedText = printed,
                        completesCheckout = true,
                    )
                )
                emitEffect(CheckoutEffect.OrderCompleted(res.value, printed))
            }
            is Result.Failure -> {
                updateState { it.copy(processing = false, error = res.error.message) }
                emitEffect(CheckoutEffect.Failed(res.error.message))
            }
        }
    }
}
