package com.coditria.footpos

import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory
import com.coditria.footpos.core.designsystem.PosAppTheme
import com.coditria.footpos.core.imageloading.buildAppImageLoader
import com.coditria.footpos.presentation.root.RootScreen
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

@Composable
fun App() {
    // Coil's singleton loader needs a PlatformContext (provided by the composable scope)
    // and our shared Ktor client. Registering once here means every AsyncImage in the
    // tree just works — no per-call configuration.
    val httpClient: HttpClient = koinInject()
    setSingletonImageLoaderFactory { context ->
        buildAppImageLoader(context, httpClient)
    }

    PosAppTheme {
        RootScreen()
    }
}
