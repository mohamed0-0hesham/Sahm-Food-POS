package com.coditria.footpos.presentation.settings

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.domain.model.AppSettings
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.TaxRate
import com.coditria.footpos.domain.usecase.ObserveSettingsUseCase
import com.coditria.footpos.domain.usecase.UpdateAutoPrintUseCase
import com.coditria.footpos.domain.usecase.UpdateCurrencyUseCase
import com.coditria.footpos.domain.usecase.UpdateStoreNameUseCase
import com.coditria.footpos.domain.usecase.UpdateTaxRateUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class SettingsState(
    val settings: AppSettings = AppSettings.DEFAULT,
    val loading: Boolean = true,
)

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val updateStoreName: UpdateStoreNameUseCase,
    private val updateTaxRate: UpdateTaxRateUseCase,
    private val updateCurrency: UpdateCurrencyUseCase,
    private val updateAutoPrint: UpdateAutoPrintUseCase,
) : MviViewModel<SettingsState, Nothing>() {

    override fun initialState() = SettingsState()

    init {
        observeSettings()
            .onEach { s -> updateState { it.copy(settings = s, loading = false) } }
            .launchIn(viewModelScope)
    }

    fun onStoreNameChanged(name: String) = launch { updateStoreName(name) }

    fun onTaxRateChanged(rate: TaxRate) = launch { updateTaxRate(rate) }

    fun onCurrencyChanged(currency: Currency) = launch { updateCurrency(currency) }

    fun onAutoPrintChanged(enabled: Boolean) = launch { updateAutoPrint(enabled) }
}
