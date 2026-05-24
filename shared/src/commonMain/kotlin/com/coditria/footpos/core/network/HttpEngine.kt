package com.coditria.footpos.core.network

import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Platform-supplied Ktor engine factory. OkHttp on Android, Darwin on iOS.
 * Wired in [com.coditria.footpos.di.PlatformModule] via [buildHttpClient].
 */
expect fun httpEngineFactory(): HttpClientEngineFactory<*>
