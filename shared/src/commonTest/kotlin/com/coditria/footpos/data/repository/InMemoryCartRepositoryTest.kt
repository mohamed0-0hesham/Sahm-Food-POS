package com.coditria.footpos.data.repository

import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryCartRepositoryTest {

    private fun product(id: String = "p1", priceCents: Long = 1000) =
        Product(ProductId(id), "Item", Money(priceCents, Currency.EGP))

    @Test
    fun `adding the same product merges quantity`() = runTest {
        val c = InMemoryCartRepository(FakeSettingsRepository(AppSettings.DEFAULT), backgroundScope)
        val p = product()
        c.addItem(p, 2)
        c.addItem(p, 3)
        val snapshot = c.snapshot()
        assertEquals(1, snapshot.items.size)
        assertEquals(5, snapshot.items.first().quantity)
    }

    @Test
    fun `updateQuantity to zero removes the line`() = runTest {
        val c = InMemoryCartRepository(FakeSettingsRepository(AppSettings.DEFAULT), backgroundScope)
        c.addItem(product(), 2)
        c.updateQuantity("p1", 0)
        assertTrue(c.snapshot().items.isEmpty())
    }

    @Test
    fun `clear resets to a fresh draft but keeps tax rate`() = runTest {
        val settings = FakeSettingsRepository(AppSettings.DEFAULT.copy(taxRate = TaxRate(0.10)))
        val c = InMemoryCartRepository(settings, backgroundScope)
        testScheduler.runCurrent()  // let the initial tax-rate emission land
        c.addItem(product(), 1)
        c.applyDiscount(Discount(Money(500)))
        c.clear()
        val s = c.snapshot()
        assertTrue(s.items.isEmpty())
        assertEquals(Money.ZERO, s.discount.amount)
        assertEquals(TaxRate(0.10), s.taxRate)
    }

    @Test
    fun `subtotal aggregates line totals`() = runTest {
        val c = InMemoryCartRepository(FakeSettingsRepository(AppSettings.DEFAULT), backgroundScope)
        c.addItem(product("a", 1500), 2)  // 3000
        c.addItem(product("b", 250), 4)   // 1000
        assertEquals(Money(4000), c.snapshot().subtotal)
    }

    @Test
    fun `cart adopts updated tax rate from settings`() = runTest {
        val settings = FakeSettingsRepository(AppSettings.DEFAULT.copy(taxRate = TaxRate(0.10)))
        val c = InMemoryCartRepository(settings, backgroundScope)
        testScheduler.runCurrent()
        assertEquals(TaxRate(0.10), c.snapshot().taxRate)
        settings.emit(AppSettings.DEFAULT.copy(taxRate = TaxRate(0.20)))
        testScheduler.runCurrent()
        assertEquals(TaxRate(0.20), c.snapshot().taxRate)
    }

    private class FakeSettingsRepository(initial: AppSettings) : SettingsRepository {
        private val state = MutableStateFlow(initial)
        fun emit(next: AppSettings) { state.value = next }
        override fun observe(): Flow<AppSettings> = state.asStateFlow()
        override suspend fun current(): AppSettings = state.value
        override suspend fun setStoreName(name: String) { state.value = state.value.copy(storeName = name) }
        override suspend fun setTaxRate(rate: TaxRate) { state.value = state.value.copy(taxRate = rate) }
        override suspend fun setCurrency(currency: Currency) { state.value = state.value.copy(currency = currency) }
        override suspend fun setAutoPrintReceipts(enabled: Boolean) { state.value = state.value.copy(autoPrintReceipts = enabled) }
    }
}
