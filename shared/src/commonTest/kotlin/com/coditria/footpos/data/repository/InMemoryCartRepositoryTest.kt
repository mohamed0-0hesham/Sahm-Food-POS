package com.coditria.footpos.data.repository

import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Lightweight settings repo for unit tests — no DataStore required. */
private class FakeSettingsRepository(initial: AppSettings = AppSettings.DEFAULT) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override fun observe(): Flow<AppSettings> = state.asStateFlow()
    override suspend fun current(): AppSettings = state.value
    override suspend fun setStoreName(name: String) { state.value = state.value.copy(storeName = name) }
    override suspend fun setTaxRate(rate: TaxRate) { state.value = state.value.copy(taxRate = rate) }
    override suspend fun setCurrency(currency: Currency) { state.value = state.value.copy(currency = currency) }
    override suspend fun setAutoPrintReceipts(enabled: Boolean) { state.value = state.value.copy(autoPrintReceipts = enabled) }
}

class InMemoryCartRepositoryTest {

    private fun product(id: String = "p1", priceCents: Long = 1000) =
        Product(ProductId(id), "Item", Money(priceCents, Currency.EGP))

    private fun newCart(scope: CoroutineScope): InMemoryCartRepository =
        InMemoryCartRepository(settings = FakeSettingsRepository(), scope = scope)

    @Test
    fun `adding the same product merges quantity`() = runTest {
        val cart = newCart(TestScope())
        val p = product()
        cart.addItem(p, 2)
        cart.addItem(p, 3)
        val snapshot = cart.snapshot()
        assertEquals(1, snapshot.items.size)
        assertEquals(5, snapshot.items.first().quantity)
    }

    @Test
    fun `updateQuantity to zero removes the line`() = runTest {
        val cart = newCart(TestScope())
        cart.addItem(product(), 2)
        cart.updateQuantity("p1", 0)
        assertTrue(cart.snapshot().items.isEmpty())
    }

    @Test
    fun `clear resets to a fresh draft`() = runTest {
        val cart = newCart(TestScope())
        cart.addItem(product(), 1)
        cart.applyDiscount(Discount(Money(500)))
        cart.clear()
        val s = cart.snapshot()
        assertTrue(s.items.isEmpty())
        assertEquals(Money.ZERO, s.discount.amount)
    }

    @Test
    fun `subtotal aggregates line totals`() = runTest {
        val cart = newCart(TestScope())
        cart.addItem(product("a", 1500), 2)  // 3000
        cart.addItem(product("b", 250), 4)   // 1000
        assertEquals(Money(4000), cart.snapshot().subtotal)
    }
}
