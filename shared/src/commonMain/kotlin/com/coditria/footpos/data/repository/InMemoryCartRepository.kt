package com.coditria.footpos.data.repository

import com.coditria.footpos.core.common.now
import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderItem
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.repository.CartRepository
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * In-memory cart. Single-cashier, single-cart-at-a-time POS — no need to persist drafts.
 *
 * Listens to [SettingsRepository] so the active draft's tax rate always reflects the
 * cashier's current configuration; cleared carts get the same rate.
 */
class InMemoryCartRepository(
    settings: SettingsRepository,
    scope: CoroutineScope,
) : CartRepository {

    private val state: MutableStateFlow<Order> = MutableStateFlow(
        Order.newDraft().copy(taxRate = AppSettings.DEFAULT.taxRate),
    )

    init {
        settings.observe()
            .map { it.taxRate }
            .distinctUntilChanged()
            .onEach { rate -> state.update { order -> order.copy(taxRate = rate, updatedAt = now()) } }
            .launchIn(scope)
    }

    fun stream(): StateFlow<Order> = state.asStateFlow()

    override fun observe() = stream()

    override fun snapshot(): Order = state.value

    override suspend fun addItem(product: Product, quantity: Int) {
        require(quantity > 0)
        state.update { order ->
            val existing = order.items.indexOfFirst { it.product.id == product.id }
            val newItems = if (existing >= 0) {
                order.items.mapIndexed { idx, item ->
                    if (idx == existing) item.copy(quantity = item.quantity + quantity) else item
                }
            } else {
                order.items + OrderItem(product, quantity, product.price)
            }
            order.copy(items = newItems, updatedAt = now())
        }
    }

    override suspend fun removeItem(productIdValue: String) {
        state.update { order ->
            order.copy(
                items = order.items.filterNot { it.product.id.value == productIdValue },
                updatedAt = now(),
            )
        }
    }

    override suspend fun updateQuantity(productIdValue: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productIdValue)
            return
        }
        state.update { order ->
            order.copy(
                items = order.items.map { item ->
                    if (item.product.id.value == productIdValue) item.copy(quantity = quantity) else item
                },
                updatedAt = now(),
            )
        }
    }

    override suspend fun applyDiscount(discount: Discount) {
        state.update { it.copy(discount = discount, updatedAt = now()) }
    }

    override suspend fun clearDiscount() {
        state.update { it.copy(discount = Discount(Money.ZERO), updatedAt = now()) }
    }

    override suspend fun setNote(note: String?) {
        state.update { it.copy(note = note, updatedAt = now()) }
    }

    override suspend fun clear() {
        val currentRate = state.value.taxRate
        state.value = Order.newDraft().copy(taxRate = currentRate)
    }
}
