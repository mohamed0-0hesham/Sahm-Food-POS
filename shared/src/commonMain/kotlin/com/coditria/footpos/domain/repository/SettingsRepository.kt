package com.coditria.footpos.domain.repository

import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import kotlinx.coroutines.flow.Flow

interface SettingsReader {
    fun observe(): Flow<AppSettings>
    suspend fun current(): AppSettings
}

interface SettingsWriter {
    suspend fun setStoreName(name: String)
    suspend fun setTaxRate(rate: TaxRate)
    suspend fun setCurrency(currency: Currency)
    suspend fun setAutoPrintReceipts(enabled: Boolean)
}

interface SettingsRepository : SettingsReader, SettingsWriter
