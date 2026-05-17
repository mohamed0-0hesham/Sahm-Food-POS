package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repo.observe()
}

class GetSettingsUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): AppSettings = repo.current()
}

class UpdateStoreNameUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(name: String) {
        require(name.isNotBlank()) { "Store name cannot be blank" }
        repo.setStoreName(name.trim())
    }
}

class UpdateTaxRateUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(rate: TaxRate) = repo.setTaxRate(rate)
}

class UpdateCurrencyUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(currency: Currency) = repo.setCurrency(currency)
}

class UpdateAutoPrintUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setAutoPrintReceipts(enabled)
}
