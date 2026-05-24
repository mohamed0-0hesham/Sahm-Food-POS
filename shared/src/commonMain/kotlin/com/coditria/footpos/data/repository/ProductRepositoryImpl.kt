package com.coditria.footpos.data.repository

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.data.product.local.ProductLocalDataSource
import com.coditria.footpos.data.product.remote.ProductRemoteDataSource
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

/**
 * Read-through cache: the UI observes [local], while [refresh] pulls a fresh snapshot
 * from [remote] and writes it into [local] in one transaction. Splitting local + remote
 * keeps the repository ignorant of SQLDelight and Ktor — both are wire-format details
 * confined to their respective data-source implementations.
 */
class ProductRepositoryImpl(
    private val local: ProductLocalDataSource,
    private val remote: ProductRemoteDataSource,
) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> = local.observeAll()
    override fun observeCategories(): Flow<List<String>> = local.observeCategories()
    override suspend fun getById(id: ProductId): Product? = local.getById(id)
    override suspend fun search(query: String): List<Product> = local.search(query)

    override suspend fun upsert(product: Product): Result<Unit> = local.upsert(product)

    override suspend fun refresh(): Result<Unit> = when (val r = remote.fetchAll()) {
        is Result.Success -> local.upsertAll(r.value)
        is Result.Failure -> r
    }
}
