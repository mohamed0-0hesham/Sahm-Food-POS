package com.coditria.footpos.presentation.orders

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.core.navigation.Destination
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.usecase.CancelOrderUseCase
import com.coditria.footpos.domain.usecase.GetOrderByIdUseCase
import com.coditria.footpos.domain.usecase.PrintReceiptUseCase
import com.coditria.footpos.domain.usecase.RetryOrderSyncUseCase
import com.coditria.footpos.presentation.shared.MviViewModel

data class OrderDetailState(
    val order: Order? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface OrderDetailEffect {
    data class Printed(val text: String) : OrderDetailEffect
    data class PrintFailed(val message: String) : OrderDetailEffect
    object Cancelled : OrderDetailEffect
}

class OrderDetailViewModel(
    private val orderId: OrderId,
    private val getOrderById: GetOrderByIdUseCase,
    private val printReceipt: PrintReceiptUseCase,
    private val retrySync: RetryOrderSyncUseCase,
    private val cancelOrder: CancelOrderUseCase,
) : MviViewModel<OrderDetailState, OrderDetailEffect>() {

    override fun initialState() = OrderDetailState()

    init { refresh() }

    fun refresh() = launch {
        val order = getOrderById(orderId)
        updateState { it.copy(order = order, loading = false) }
    }

    fun onReprint() = launch {
        val order = currentState.order ?: return@launch
        when (val res = printReceipt(order)) {
            is Result.Success -> emitEffect(OrderDetailEffect.Printed(res.value))
            is Result.Failure -> emitEffect(OrderDetailEffect.PrintFailed(res.error.message))
        }
    }

    fun onRetrySync() = launch {
        retrySync(orderId)
        refresh()
    }

    fun onCancel() = launch {
        cancelOrder(orderId)
        emitEffect(OrderDetailEffect.Cancelled)
    }

    @Suppress("unused")
    fun gotoReceipt(navigator: Navigator, printedText: String) {
        navigator.showModal(Destination.Modal.Receipt(orderId, printedText))
    }
}
