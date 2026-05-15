package com.coditria.footpos.domain.usecase

import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ObserveProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<List<Product>> = repository.observeAll()
}

class ObserveCategoriesUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<List<String>> = repository.observeCategories()
}

class SearchProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(query: String): List<Product> =
        if (query.isBlank()) emptyList() else repository.search(query.trim())
}
