package com.coditria.footpos.core.imageloading

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient

/**
 * Builds the single Coil [ImageLoader] used across the app. We hand Coil the same
 * Ktor [HttpClient] that powers the API layer so connection pools and HTTP-cache
 * settings are shared — no second engine just for images.
 */
fun buildAppImageLoader(
    context: PlatformContext,
    httpClient: HttpClient,
): ImageLoader = ImageLoader.Builder(context)
    .components {
        add(KtorNetworkFetcherFactory(httpClient = httpClient))
    }
    .memoryCache {
        MemoryCache.Builder()
            .maxSizePercent(context, 0.20)   // 20% of available heap is plenty for thumbnails
            .build()
    }
    .crossfade(true)
    .build()
