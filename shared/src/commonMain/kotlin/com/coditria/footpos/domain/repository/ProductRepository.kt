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

    /**
     * Pulls the catalog from the remote source of truth and persists it locally.
     * Caller decides when to invoke (app launch, pull-to-refresh). The repository
     * keeps the local cache as the read source so the UI never blocks on the network.
     */
    suspend fun refresh(): Result<Unit>
}

interface ProductRepository : ProductReader, ProductWriter
