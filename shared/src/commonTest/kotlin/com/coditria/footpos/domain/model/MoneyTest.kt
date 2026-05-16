package com.coditria.footpos.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `plus sums amounts when currencies match`() {
        val a = Money(1999, Currency.EGP)
        val b = Money(1, Currency.EGP)
        assertEquals(Money(2000, Currency.EGP), a + b)
    }

    @Test
    fun `plus rejects mismatched currencies`() {
        assertFailsWith<IllegalArgumentException> {
            Money(100, Currency.EGP) + Money(100, Currency.USD)
        }
    }

    @Test
    fun `times by int multiplies amount`() {
        assertEquals(Money(9000), Money(3000) * 3)
    }

    @Test
    fun `times by double rounds to nearest cent`() {
        // 1000 * 0.14 = 140 (whole cents)
        assertEquals(Money(140), Money(1000) * 0.14)
        // Half-up rounding through roundToLong: 33 * 0.5 -> 17
        assertEquals(Money(17), Money(33) * 0.5)
    }

    @Test
    fun `compare orders amounts numerically`() {
        assertTrue(Money(100) < Money(200))
        assertEquals(0, Money(100).compareTo(Money(100)))
    }

    @Test
    fun `format renders two-decimal output with thousands separator`() {
        assertEquals("EGP 1,234.56", Money(123456).format())
        assertEquals("EGP 0.05", Money(5).format())
        assertEquals("-1,234.56", Money(-123456).formatAmount())
    }
}
