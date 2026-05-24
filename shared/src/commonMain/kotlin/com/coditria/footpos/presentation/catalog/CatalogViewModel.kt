package com.coditria.footpos.presentation.catalog

import androidx.lifecycle.viewModelScope
import com.coditria.footpos.core.navigation.Destination
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.network.NetworkMonitor
import com.coditria.footpos.domain.usecase.AddItemToCartUseCase
import com.coditria.footpos.domain.usecase.ObserveCartUseCase
import com.coditria.footpos.domain.usecase.ObserveCategoriesUseCase
import com.coditria.footpos.domain.usecase.ObserveProductsUseCase
import com.coditria.footpos.domain.usecase.ObserveTopSellersUseCase
import com.coditria.footpos.presentation.shared.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class CatalogState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = ALL,
    val searchQuery: String = "",
    val cart: Order = Order.newDraft(),
    val loading: Boolean = true,
    val online: Boolean = true,
    /** Top sellers as (product, units sold), highest first. */
    val bestSellers: List<Pair<Product, Int>> = emptyList(),
) {
    val visibleProducts: List<Product>
        get() {
            val q = searchQuery.trim()
            val base = if (selectedCategory == ALL) products
            else products.filter { it.category == selectedCategory }
            return if (q.isEmpty()) base
            else base.filter { it.name.contains(q, ignoreCase = true) || it.barcode?.contains(q, ignoreCase = true) == true }
        }

    companion object { const val ALL = "All" }
}

sealed interface CatalogEffect {
    data object ItemAdded : CatalogEffect
}

class CatalogViewModel(
    observeProducts: ObserveProductsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeCart: ObserveCartUseCase,
    observeTopSellers: ObserveTopSellersUseCase,
    networkMonitor: NetworkMonitor,
    private val addToCart: AddItemToCartUseCase,
    private val navigator: Navigator,
) : MviViewModel<CatalogState, CatalogEffect>() {

    override fun initialState() = CatalogState()

    init {
        // Catalog + cart + categories drive the bulk of the UI; combine them so a
        // single emission rebuilds the view cleanly with consistent state.
        combine(
            observeProducts(),
            observeCategories(),
            observeCart(),
        ) { products, cats, cart ->
            CatalogState(
                products = products,
                categories = listOf(CatalogState.ALL) + cats,
                cart = cart,
                loading = false,
                selectedCategory = currentState.selectedCategory,
                searchQuery = currentState.searchQuery,
                online = currentState.online,
                bestSellers = currentState.bestSellers,
            )
        }
            .onEach { updateState { _ -> it } }
            .launchIn(viewModelScope)

        // Hot streams that don't participate in the bulk combine — fold their values
        // into state without triggering a full rebuild on every emission.
        networkMonitor.isOnline
            .onEach { online -> updateState { it.copy(online = online) } }
            .launchIn(viewModelScope)

        observeTopSellers(limit = 5)
            .onEach { sellers -> updateState { it.copy(bestSellers = sellers) } }
            .launchIn(viewModelScope)
    }

    fun onCategorySelected(category: String) =
        updateState { it.copy(selectedCategory = category) }

    fun onSearchChanged(query: String) =
        updateState { it.copy(searchQuery = query) }

    fun onProductTapped(product: Product) = launch {
        addToCart(product)
        emitEffect(CatalogEffect.ItemAdded)
    }

    fun onProductLongPressed(product: Product) {
        navigator.showModal(Destination.Modal.ProductDetail(product.id.value))
    }

    fun onCheckoutClicked() {
        navigator.showModal(Destination.Modal.Checkout)
    }

    fun onOpenCartPhone() {
        navigator.push(Destination.Stacked.CartPhone)
    }
}
