package com.coditria.footpos.domain.model

import kotlinx.datetime.Instant

data class ReceiptLine(
    val name: String,
    val quantity: Int,
    val unitPrice: Money,
    val total: Money,
)

data class Receipt(
    val orderId: OrderId,
    val items: List<ReceiptLine>,
    val subtotal: Money,
    val tax: Money,
    val discount: Money,
    val total: Money,
    val timestamp: Instant,
    val payment: Payment?,
    val merchantName: String = "Sahm Food",
) {
    companion object {
        fun fromOrder(order: Order): Receipt = Receipt(
            orderId = order.id,
            items = order.items.map { ReceiptLine(it.product.name, it.quantity, it.unitPrice, it.subtotal) },
            subtotal = order.subtotal,
            tax = order.taxAmount,
            discount = order.discount.amount,
            total = order.total,
            timestamp = order.updatedAt,
            payment = order.payment,
        )
    }
}

enum class PrintResult { Success, Offline, Failed }
