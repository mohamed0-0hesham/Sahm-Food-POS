package com.coditria.footpos.di

import com.coditria.footpos.core.database.DatabaseDriverFactory
import com.coditria.footpos.data.auth.AndroidGoogleAuthClient
import com.coditria.footpos.data.auth.UnsupportedAppleAuthClient
import com.coditria.footpos.data.network.AndroidNetworkMonitor
import com.coditria.footpos.data.settings.SettingsFactory
import com.coditria.footpos.domain.auth.AppleAuthClient
import com.coditria.footpos.domain.auth.GoogleAuthClient
import com.coditria.footpos.domain.network.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
    single { SettingsFactory(androidContext()) }
    single<GoogleAuthClient> { AndroidGoogleAuthClient() }
    single<AppleAuthClient> { UnsupportedAppleAuthClient() }
}
