package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.OrderItem
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.model.TaxRate
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateOrderTotalUseCaseTest {

    private val useCase = CalculateOrderTotalUseCase()

    private fun item(name: String, priceCents: Long, quantity: Int) = OrderItem(
        product = Product(ProductId(name), name, Money(priceCents, Currency.EGP)),
        quantity = quantity,
        unitPrice = Money(priceCents, Currency.EGP),
    )

    @Test
    fun `applies tax to discounted subtotal`() {
        val totals = useCase(
            items = listOf(item("Burger", 10000, 2)),
            discount = Discount(Money(2000)),
            taxRate = TaxRate(0.14),
        )
        assertEquals(Money(20000), totals.subtotal)
        assertEquals(Money(2000), totals.discount)
        assertEquals(Money(2800), totals.tax)
        assertEquals(Money(20800), totals.total)
    }

    @Test
    fun `empty cart yields zero totals`() {
        val totals = useCase(items = emptyList(), discount = Discount(), taxRate = TaxRate.EGYPT_VAT)
        assertEquals(Money.ZERO, totals.subtotal)
        assertEquals(Money.ZERO, totals.total)
    }

    @Test
    fun `zero discount produces subtotal plus tax`() {
        val totals = useCase(
            items = listOf(item("Coke", 1000, 3)),
            discount = Discount(),
            taxRate = TaxRate(0.10),
        )
        assertEquals(Money(3000), totals.subtotal)
        assertEquals(Money(300), totals.tax)
        assertEquals(Money(3300), totals.total)
    }
}
