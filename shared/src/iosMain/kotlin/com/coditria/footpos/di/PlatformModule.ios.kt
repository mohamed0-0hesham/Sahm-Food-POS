package com.coditria.footpos.di

import com.coditria.footpos.core.database.DatabaseDriverFactory
import com.coditria.footpos.data.network.IosNetworkMonitor
import com.coditria.footpos.domain.network.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<NetworkMonitor> { IosNetworkMonitor() }
}
