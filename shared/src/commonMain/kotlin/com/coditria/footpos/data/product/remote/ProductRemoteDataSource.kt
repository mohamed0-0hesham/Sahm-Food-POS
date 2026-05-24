package com.coditria.footpos.data.product.remote

import com.coditria.footpos.core.common.Result
import com.coditria.footpos.domain.model.Product

/**
 * Backend port for the product catalog. The repository depends on this interface,
 * not the Ktor client, so the API (DummyJSON today, Firestore / Supabase / internal
 * service tomorrow) is a single Koin binding change.
 */
interface ProductRemoteDataSource {
    suspend fun fetchAll(): Result<List<Product>>
}
