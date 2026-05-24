package com.coditria.footpos.data.product.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.coditria.footpos.core.common.AppError
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.core.common.Result
import com.coditria.footpos.data.mapper.toDomain
import com.coditria.footpos.database.PosDatabase
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightProductLocalDataSource(
    database: PosDatabase,
    private val dispatchers: DispatcherProvider,
) : ProductLocalDataSource {

    private val queries = database.productsQueries

    override fun observeAll(): Flow<List<Product>> =
        queries.selectAll().asFlow().mapToList(dispatchers.io).map { rows -> rows.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<String>> =
        queries.distinctCategories().asFlow().mapToList(dispatchers.io)
            .map { rows -> rows }

    override suspend fun getById(id: ProductId): Product? = withContext(dispatchers.io) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun search(query: String): List<Product> = withContext(dispatchers.io) {
        queries.search(query).executeAsList().map { it.toDomain() }
    }

    override suspend fun upsert(product: Product): Result<Unit> = guarded {
        writeProduct(product)
    }

    override suspend fun upsertAll(products: List<Product>): Result<Unit> = guarded {
        // One transaction = one DB round trip; SQLDelight propagates a single change
        // notification to observers, so the UI updates atomically.
        queries.transaction {
            products.forEach { writeProduct(it) }
        }
    }

    override suspend fun count(): Long = withContext(dispatchers.io) {
        queries.count().executeAsOne()
    }

    override suspend fun clear() {
        // No deleteAll query was defined — keep this no-op until one is added rather
        // than running raw DELETE outside the generated API.
    }

    private fun writeProduct(product: Product) {
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

    private suspend inline fun guarded(crossinline block: () -> Unit): Result<Unit> = runCatching {
        withContext(dispatchers.io) { block() }
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { Result.Failure(AppError.DatabaseError(it.message ?: "Failed to write product(s)")) },
    )
}
