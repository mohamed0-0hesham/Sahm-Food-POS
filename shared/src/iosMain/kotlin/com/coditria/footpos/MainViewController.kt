package com.coditria.footpos

import androidx.compose.ui.window.ComposeUIViewController
import com.coditria.footpos.di.KoinInitializer
import org.koin.core.context.GlobalContext

fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) {
        KoinInitializer.init()
    }
    App()
}
