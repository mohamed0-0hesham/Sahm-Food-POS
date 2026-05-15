package com.coditria.footpos.domain.model

import com.coditria.footpos.core.common.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

@JvmInline
value class OrderId(val value: String) {
    companion object {
        fun generate(): OrderId = OrderId(Uuid.random())
    }
}

enum class OrderStatus { DRAFT, CONFIRMED, PAID, CANCELLED }

enum class SyncStatus { PENDING, SYNCED, FAILED }

enum class PaymentMethod { CASH, CARD, OTHER }

data class Discount(
    val amount: Money = Money.ZERO,
    val reason: String? = null,
)

data class OrderItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Money,
) {
    init {
        require(quantity > 0) { "Quantity must be positive, was $quantity" }
    }
    val subtotal: Money get() = unitPrice * quantity
}

data class OrderTotals(
    val subtotal: Money,
    val discount: Money,
    val tax: Money,
    val total: Money,
)

data class Payment(
    val method: PaymentMethod,
    val amountTendered: Money,
) {
    fun changeDue(total: Money): Money {
        val diff = amountTendered.amountInCents - total.amountInCents
        return Money(if (diff > 0) diff else 0, amountTendered.currency)
    }
}

data class Order(
    val id: OrderId,
    val items: List<OrderItem>,
    val discount: Discount = Discount(),
    val taxRate: TaxRate = TaxRate.EGYPT_VAT,
    val status: OrderStatus = OrderStatus.DRAFT,
    val note: String? = null,
    val payment: Payment? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
) {
    val subtotal: Money
        get() = items.fold(Money.ZERO) { acc, item -> acc + item.subtotal }

    val taxAmount: Money get() = subtotal * taxRate.value

    val total: Money get() = subtotal - discount.amount + taxAmount

    val totals: OrderTotals
        get() = OrderTotals(
            subtotal = subtotal,
            discount = discount.amount,
            tax = taxAmount,
            total = total,
        )

    companion object {
        fun newDraft(now: Instant = Clock.System.now()): Order = Order(
            id = OrderId.generate(),
            items = emptyList(),
            createdAt = now,
            updatedAt = now,
        )
    }
}
