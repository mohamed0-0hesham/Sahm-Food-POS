package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.OrderItem
import com.coditria.footpos.domain.model.OrderTotals
import com.coditria.footpos.domain.model.TaxRate

/**
 * Pure calculation. Kept as a separate use case to make tax/discount logic
 * trivially testable in isolation, independent of any repository.
 */
class CalculateOrderTotalUseCase {
    operator fun invoke(
        items: List<OrderItem>,
        discount: Discount = Discount(),
        taxRate: TaxRate = TaxRate.EGYPT_VAT,
    ): OrderTotals {
        val subtotal = items.fold(Money.ZERO) { acc, item -> acc + item.subtotal }
        val tax = subtotal * taxRate.value
        val total = subtotal - discount.amount + tax
        return OrderTotals(subtotal = subtotal, discount = discount.amount, tax = tax, total = total)
    }
}
