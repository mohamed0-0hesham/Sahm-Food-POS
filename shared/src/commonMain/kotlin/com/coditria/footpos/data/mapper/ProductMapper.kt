package com.coditria.footpos.data.mapper

import com.coditria.footpos.database.ProductEntity
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId

internal fun ProductEntity.toDomain(): Product = Product(
    id = ProductId(id),
    name = name,
    price = Money(priceCents, Currency.valueOf(currency)),
    description = description,
    category = category,
    barcode = barcode,
    imageUrl = imageUrl,
    emoji = emoji,
)
