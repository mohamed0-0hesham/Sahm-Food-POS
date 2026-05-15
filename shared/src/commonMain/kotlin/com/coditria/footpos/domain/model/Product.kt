package com.coditria.footpos.domain.model

import com.coditria.footpos.core.common.Uuid
import kotlin.jvm.JvmInline

@JvmInline
value class ProductId(val value: String) {
    companion object {
        fun generate(): ProductId = ProductId(Uuid.random())
    }
}

data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
    val description: String? = null,
    val category: String? = null,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val emoji: String? = null,
)
