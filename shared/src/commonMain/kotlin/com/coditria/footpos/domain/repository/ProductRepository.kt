package com.coditria.footpos.domain.repository

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import kotlinx.coroutines.flow.Flow

interface ProductReader {
    fun observeAll(): Flow<List<Product>>
    fun observeCategories(): Flow<List<String>>
    suspend fun getById(id: ProductId): Product?
    suspend fun search(query: String): List<Product>
}

interface ProductWriter {
    suspend fun upsert(product: Product): Result<Unit>
}

interface ProductRepository : ProductReader, ProductWriter
