package com.coditria.footpos.data.repository

import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.repository.SettingsRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Persists settings via the multiplatform [Settings] facade (SharedPreferences on Android,
 * NSUserDefaults on iOS) and exposes a hot [Flow] for observers.
 *
 * Storage keys live as private constants — they never leak into the domain.
 *
 * Library-flavoured Settings is synchronous, so writes happen immediately and the in-memory
 * [MutableStateFlow] is updated in lock-step. This avoids needing the experimental
 * FlowSettings adapter and keeps the public API of SettingsRepository fully suspending.
 */
class SettingsRepositoryImpl(
    private val settings: Settings,
) : SettingsRepository {

    private val state: MutableStateFlow<AppSettings> = MutableStateFlow(readSnapshot())

    override fun observe(): Flow<AppSettings> = state.asStateFlow()

    override suspend fun current(): AppSettings = state.value

    override suspend fun setStoreName(name: String) {
        settings.putString(Keys.StoreName, name)
        state.update { it.copy(storeName = name) }
    }

    override suspend fun setTaxRate(rate: TaxRate) {
        settings.putDouble(Keys.TaxRate, rate.value)
        state.update { it.copy(taxRate = rate) }
    }

    override suspend fun setCurrency(currency: Currency) {
        settings.putString(Keys.Currency, currency.name)
        state.update { it.copy(currency = currency) }
    }

    override suspend fun setAutoPrintReceipts(enabled: Boolean) {
        settings.putBoolean(Keys.AutoPrint, enabled)
        state.update { it.copy(autoPrintReceipts = enabled) }
    }

    /** Reads the persisted blob into an [AppSettings], falling back to DEFAULT for missing keys. */
    private fun readSnapshot(): AppSettings {
        val defaults = AppSettings.DEFAULT
        val taxValue = settings.getDoubleOrNull(Keys.TaxRate)?.coerceIn(0.0, 1.0)
        val currencyName = settings.getStringOrNull(Keys.Currency)
        val parsedCurrency = currencyName?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
        return AppSettings(
            storeName = settings.getStringOrNull(Keys.StoreName) ?: defaults.storeName,
            taxRate = taxValue?.let(::TaxRate) ?: defaults.taxRate,
            currency = parsedCurrency ?: defaults.currency,
            autoPrintReceipts = settings.getBooleanOrNull(Keys.AutoPrint) ?: defaults.autoPrintReceipts,
        )
    }

    private object Keys {
        const val StoreName = "store_name"
        const val TaxRate = "tax_rate"
        const val Currency = "currency"
        const val AutoPrint = "auto_print"
    }
}
