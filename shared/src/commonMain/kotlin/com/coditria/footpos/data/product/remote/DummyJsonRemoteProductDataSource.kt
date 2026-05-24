package com.coditria.footpos.data.product.remote

import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.data.product.remote.dto.DummyJsonProductsResponse
import com.coditria.footpos.data.product.remote.mapper.toDomain
import com.coditria.footpos.domain.model.Product
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Pulls the catalog from https://dummyjson.com/products .
 *
 * The endpoint is paged but defaults to 30 items; for a POS demo we just ask for a
 * larger page rather than implementing pagination. Errors are flattened into
 * [Result.Failure] with [AppError.NetworkError] so upstream code doesn't have to
 * catch Ktor exceptions.
 */
class DummyJsonRemoteProductDataSource(
    private val client: HttpClient,
    private val logger: Logger,
) : ProductRemoteDataSource {

    override suspend fun fetchAll(): Result<List<Product>> = runCatching {
        val response: DummyJsonProductsResponse = client
            .get(BASE_URL) { parameter("limit", PAGE_SIZE) }
            .body()
        response.products.map { it.toDomain() }
    }.fold(
        onSuccess = { products ->
            logger.info("Fetched ${products.size} products from DummyJSON")
            Result.Success(products)
        },
        onFailure = { t ->
            logger.warn("DummyJSON fetch failed: ${t.message}", t)
            Result.Failure(AppError.NetworkError(t.message ?: "Failed to fetch products"))
        },
    )

    private companion object {
        const val BASE_URL = "https://dummyjson.com/products"
        const val PAGE_SIZE = 100
    }
}
