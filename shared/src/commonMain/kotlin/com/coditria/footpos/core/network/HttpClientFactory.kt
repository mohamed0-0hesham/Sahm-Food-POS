package com.coditria.footpos.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Common Ktor configuration. Platform code builds the actual client by passing the
 * appropriate engine factory (OkHttp on Android, Darwin on iOS) — this keeps the
 * engine choice at the edge and the configuration (JSON, timeouts, logging) shared.
 */
object HttpClientFactory {

    private val json = Json {
        ignoreUnknownKeys = true   // tolerate API additions
        isLenient = true
        explicitNulls = false
    }

    fun configure(
        config: HttpClientConfig<*>,
        logger: KtorLogger,
    ) {
        config.install(ContentNegotiation) { json(json) }
        config.install(Logging) {
            this.logger = logger
            level = LogLevel.INFO
        }
        config.install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        config.defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

/** Bridges our Logger port to Ktor's logger interface so HTTP traces land in the app log. */
class KtorAppLogger(
    private val app: com.coditria.footpos.core.common.Logger,
) : KtorLogger {
    override fun log(message: String) {
        app.debug(message)
    }
}

/** Built by platform modules. We expose a typed wrapper so call sites don't import Ktor directly. */
fun buildHttpClient(
    engineFactory: io.ktor.client.engine.HttpClientEngineFactory<*>,
    logger: com.coditria.footpos.core.common.Logger,
): HttpClient = HttpClient(engineFactory) {
    HttpClientFactory.configure(this, KtorAppLogger(logger))
}
