package com.coditria.footpos.core.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun httpEngineFactory(): HttpClientEngineFactory<*> = Darwin
