package com.coditria.footpos.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.data.mapper.toDomain
import com.coditria.footpos.database.PosDatabase
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val database: PosDatabase,
    private val dispatchers: DispatcherProvider,
) : ProductRepository {

    private val queries = database.productsQueries

    override fun observeAll(): Flow<List<Product>> =
        queries.selectAll().asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<String>> =
        queries.distinctCategories().asFlow().mapToList(dispatchers.io)
            .map { it.mapNotNull { row -> row.category } }

    override suspend fun getById(id: ProductId): Product? = withContext(dispatchers.io) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun search(query: String): List<Product> = withContext(dispatchers.io) {
        queries.search(query).executeAsList().map { it.toDomain() }
    }

    override suspend fun upsert(product: Product): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            queries.upsert(
                id = product.id.value,
                name = product.name,
                description = product.description,
                priceCents = product.price.amountInCents,
                currency = product.price.currency.name,
                category = product.category,
                barcode = product.barcode,
                imageUrl = product.imageUrl,
                emoji = product.emoji,
            )
        }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Failure(AppError.DatabaseError(it.message ?: "Failed to save product")) },
    )
}
