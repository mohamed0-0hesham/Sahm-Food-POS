package com.coditria.footpos.domain.model

import kotlin.jvm.JvmInline
import kotlin.math.roundToLong

enum class Currency(val code: String, val symbol: String) {
    EGP("EGP", "EGP"),
    USD("USD", "$"),
}

/**
 * Money is stored as an integer count of the currency's minor unit (cents/piastres).
 * Avoids floating-point rounding errors entirely. Only converted to Double at the UI edge.
 */
data class Money(
    val amountInCents: Long,
    val currency: Currency = Currency.EGP,
) : Comparable<Money> {

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return Money(amountInCents + other.amountInCents, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return Money(amountInCents - other.amountInCents, currency)
    }

    operator fun times(multiplier: Int): Money = Money(amountInCents * multiplier, currency)

    operator fun times(multiplier: Double): Money =
        Money((amountInCents * multiplier).roundToLong(), currency)

    operator fun unaryMinus(): Money = Money(-amountInCents, currency)

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "Currency mismatch: $currency vs ${other.currency}" }
        return amountInCents.compareTo(other.amountInCents)
    }

    fun isZero(): Boolean = amountInCents == 0L
    fun isPositive(): Boolean = amountInCents > 0L

    /** Formats as `1,234.56` without the currency code (UI decides where to place that). */
    fun formatAmount(): String {
        val negative = amountInCents < 0L
        val abs = if (negative) -amountInCents else amountInCents
        val whole = abs / 100
        val cents = abs % 100
        val wholeStr = whole.toString().reversed().chunked(3).joinToString(",").reversed()
        val centsStr = if (cents < 10) "0$cents" else cents.toString()
        return (if (negative) "-" else "") + "$wholeStr.$centsStr"
    }

    /** Formats as `EGP 1,234.56`. */
    fun format(): String = "${currency.symbol} ${formatAmount()}"

    companion object {
        val ZERO = Money(0, Currency.EGP)
        fun egp(major: Long): Money = Money(major * 100, Currency.EGP)
        fun fromCents(cents: Long, currency: Currency = Currency.EGP): Money = Money(cents, currency)
    }
}

@JvmInline
value class TaxRate(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Tax rate must be between 0.0 and 1.0, was $value" }
    }
    companion object {
        val ZERO = TaxRate(0.0)
        val EGYPT_VAT = TaxRate(0.14)
    }
}
