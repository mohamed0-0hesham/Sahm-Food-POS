package com.coditria.footpos.data.product.local

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import kotlinx.coroutines.flow.Flow

/**
 * Persistent product cache. Today this is SQLDelight; tomorrow it could be Room or a
 * different store. The repository depends on this port so swapping the engine is
 * a single Koin binding change.
 */
interface ProductLocalDataSource {
    fun observeAll(): Flow<List<Product>>
    fun observeCategories(): Flow<List<String>>
    suspend fun getById(id: ProductId): Product?
    suspend fun search(query: String): List<Product>
    suspend fun upsert(product: Product): Result<Unit>
    suspend fun upsertAll(products: List<Product>): Result<Unit>
    suspend fun count(): Long
    suspend fun clear()
}
