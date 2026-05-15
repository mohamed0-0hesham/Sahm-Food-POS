package com.coditria.footpos.domain.repository

import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Holds the cashier's in-progress order. The cart is in-memory and ephemeral by design —
 * an unfinished cart should not persist across app restarts.
 */
interface CartRepository {
    fun observe(): Flow<Order>
    fun snapshot(): Order
    suspend fun addItem(product: Product, quantity: Int = 1)
    suspend fun removeItem(productIdValue: String)
    suspend fun updateQuantity(productIdValue: String, quantity: Int)
    suspend fun applyDiscount(discount: Discount)
    suspend fun clearDiscount()
    suspend fun setNote(note: String?)
    suspend fun clear()
}
