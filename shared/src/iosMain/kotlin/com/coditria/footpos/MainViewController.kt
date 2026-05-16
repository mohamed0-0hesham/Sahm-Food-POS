package com.coditria.footpos

import androidx.compose.ui.window.ComposeUIViewController
import com.coditria.footpos.di.KoinInitializer
import org.koin.mp.KoinPlatform

private val koinStarted = atomicLazyOnce { KoinInitializer.init() }

private fun atomicLazyOnce(initializer: () -> Unit): () -> Unit {
    var done = false
    return {
        if (!done) {
            done = true
            initializer()
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    runCatching { KoinPlatform.getKoin() }.getOrElse { koinStarted() }
    App()
}
