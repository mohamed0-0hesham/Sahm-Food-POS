package com.coditria.footpos.domain.model

/**
 * Persistent app configuration. Stored in key-value preferences in the data layer;
 * the domain doesn't know about the storage mechanism.
 */
data class AppSettings(
    val storeName: String,
    val taxRate: TaxRate,
    val currency: Currency,
    val autoPrintReceipts: Boolean,
) {
    companion object {
        val DEFAULT = AppSettings(
            storeName = "Sahm Food Demo",
            taxRate = TaxRate.EGYPT_VAT,
            currency = Currency.EGP,
            autoPrintReceipts = true,
        )
    }
}
