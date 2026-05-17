package com.coditria.footpos.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed settings. Keys are private to this file so storage details
 * never leak into the domain.
 */
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observe(): Flow<AppSettings> =
        dataStore.data.map { prefs -> prefs.toAppSettings() }

    override suspend fun current(): AppSettings = observe().first()

    override suspend fun setStoreName(name: String) {
        dataStore.edit { it[Keys.StoreName] = name }
    }

    override suspend fun setTaxRate(rate: TaxRate) {
        dataStore.edit { it[Keys.TaxRate] = rate.value }
    }

    override suspend fun setCurrency(currency: Currency) {
        dataStore.edit { it[Keys.Currency] = currency.name }
    }

    override suspend fun setAutoPrintReceipts(enabled: Boolean) {
        dataStore.edit { it[Keys.AutoPrint] = enabled }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings.DEFAULT
        return AppSettings(
            storeName = this[Keys.StoreName] ?: defaults.storeName,
            taxRate = this[Keys.TaxRate]?.let { TaxRate(it.coerceIn(0.0, 1.0)) } ?: defaults.taxRate,
            currency = this[Keys.Currency]?.let { runCatching { Currency.valueOf(it) }.getOrNull() }
                ?: defaults.currency,
            autoPrintReceipts = this[Keys.AutoPrint] ?: defaults.autoPrintReceipts,
        )
    }

    private object Keys {
        val StoreName = stringPreferencesKey("store_name")
        val TaxRate = doublePreferencesKey("tax_rate")
        val Currency = stringPreferencesKey("currency")
        val AutoPrint = booleanPreferencesKey("auto_print")
    }
}
