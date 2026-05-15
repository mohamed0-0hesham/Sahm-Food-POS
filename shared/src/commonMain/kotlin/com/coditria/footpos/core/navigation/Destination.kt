package com.coditria.footpos.core.navigation

import com.coditria.footpos.domain.model.OrderId

sealed interface Destination {
    sealed interface Tab : Destination {
        object Sell : Tab
        object Orders : Tab
        object Settings : Tab
    }
    sealed interface Stacked : Destination {
        object CartPhone : Stacked
        data class OrderDetail(val orderId: OrderId) : Stacked
        object About : Stacked
    }
    sealed interface Modal : Destination {
        object Checkout : Modal
        data class Receipt(val orderId: OrderId, val printedText: String?) : Modal
        object Discount : Modal
        object SyncStatus : Modal
        data class ProductDetail(val productIdValue: String) : Modal
    }
}

enum class TabKey { Sell, Orders, Settings }

fun Destination.Tab.key(): TabKey = when (this) {
    Destination.Tab.Sell -> TabKey.Sell
    Destination.Tab.Orders -> TabKey.Orders
    Destination.Tab.Settings -> TabKey.Settings
}
