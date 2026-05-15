package com.coditria.footpos.data.mapper

import com.coditria.footpos.database.OrderEntity
import com.coditria.footpos.database.OrderItemEntity
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Discount
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Order
import com.coditria.footpos.domain.model.OrderId
import com.coditria.footpos.domain.model.OrderItem
import com.coditria.footpos.domain.model.OrderStatus
import com.coditria.footpos.domain.model.Payment
import com.coditria.footpos.domain.model.PaymentMethod
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.model.SyncStatus
import com.coditria.footpos.domain.model.TaxRate
import kotlinx.datetime.Instant

internal fun OrderEntity.toDomain(items: List<OrderItem>): Order = Order(
    id = OrderId(id),
    items = items,
    discount = Discount(Money(discountCents), discountReason),
    taxRate = TaxRate(taxRate),
    status = OrderStatus.valueOf(status),
    note = note,
    payment = paymentMethod?.let { method ->
        Payment(
            method = PaymentMethod.valueOf(method),
            amountTendered = Money(paymentAmountCents ?: totalCents),
        )
    },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    syncStatus = SyncStatus.valueOf(syncStatus),
)

internal fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    product = Product(
        id = ProductId(productId),
        name = productName,
        price = Money(unitPriceCents, Currency.valueOf(currency)),
    ),
    quantity = quantity.toInt(),
    unitPrice = Money(unitPriceCents, Currency.valueOf(currency)),
)
