package com.coditria.footpos.data.product.remote.mapper

import com.coditria.footpos.data.product.remote.dto.DummyJsonProductDto
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import kotlin.math.roundToLong

/**
 * DummyJSON returns prices in USD as decimals (e.g. 9.99). We store everything as
 * EGP-cents elsewhere in the app, so we convert at the data-layer boundary using a
 * fixed display rate. This keeps the domain Money invariant (single currency in,
 * single currency out) regardless of where the data came from.
 *
 * The conversion rate is intentionally hard-coded for the demo — when a real backend
 * lands, currency conversion belongs to it (or to a dedicated FX service), not here.
 */
private const val USD_TO_EGP = 50.0

internal fun DummyJsonProductDto.toDomain(): Product = Product(
    id = ProductId("dummyjson-$id"),
    name = title,
    price = Money(
        amountInCents = (price * USD_TO_EGP * 100).roundToLong(),
        currency = Currency.EGP,
    ),
    description = description,
    category = category?.replaceFirstChar { it.uppercaseChar() },
    barcode = sku,
    imageUrl = thumbnail ?: images.firstOrNull(),
    // No emoji from the API; CategoryGlyph fallback handles UI presentation elsewhere.
    emoji = null,
)
