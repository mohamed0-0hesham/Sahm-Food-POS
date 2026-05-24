package com.coditria.footpos.di

import com.coditria.footpos.core.database.DatabaseDriverFactory
import com.coditria.footpos.data.auth.IosAppleAuthClient
import com.coditria.footpos.data.auth.IosGoogleAuthClient
import com.coditria.footpos.data.network.IosNetworkMonitor
import com.coditria.footpos.data.settings.SettingsFactory
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.network.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<NetworkMonitor> { IosNetworkMonitor() }
    single { SettingsFactory() }
    single<GoogleAuthClient> { IosGoogleAuthClient() }
    single<AppleAuthClient> { IosAppleAuthClient() }
}
