package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class ObserveCartUseCase(private val cart: CartRepository) {
    operator fun invoke(): Flow<Order> = cart.observe()
}

class AddItemToCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(product: Product, quantity: Int = 1) {
        require(quantity > 0) { "Quantity must be positive" }
        cart.addItem(product, quantity)
    }
}

class RemoveItemFromCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(productIdValue: String) = cart.removeItem(productIdValue)
}

class UpdateItemQuantityUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(productIdValue: String, quantity: Int) {
        if (quantity <= 0) {
            cart.removeItem(productIdValue)
        } else {
            cart.updateQuantity(productIdValue, quantity)
        }
    }
}

class ApplyDiscountUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(amount: Money, reason: String? = null) {
        require(!amount.amountInCents.let { it < 0 }) { "Discount cannot be negative" }
        if (amount.isZero()) cart.clearDiscount() else cart.applyDiscount(Discount(amount, reason))
    }
}

class SetOrderNoteUseCase(private val cart: CartRepository) {
    suspend operator fun invoke(note: String?) = cart.setNote(note?.takeIf { it.isNotBlank() })
}

class ClearCartUseCase(private val cart: CartRepository) {
    suspend operator fun invoke() = cart.clear()
}
