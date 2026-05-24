package com.coditria.footpos.data.product.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire shape returned by https://dummyjson.com/products .
 * Kept intentionally permissive — unknown JSON keys are ignored by the
 * [HttpClientFactory] JSON config, so the API can grow without breaking us.
 */
@Serializable
internal data class DummyJsonProductsResponse(
    val products: List<DummyJsonProductDto> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0,
)

@Serializable
internal data class DummyJsonProductDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: Double,                       // in major units (USD)
    val category: String? = null,
    val thumbnail: String? = null,
    @SerialName("images") val images: List<String> = emptyList(),
    @SerialName("sku") val sku: String? = null,
)
