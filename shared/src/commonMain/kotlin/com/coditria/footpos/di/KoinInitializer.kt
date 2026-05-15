package com.coditria.footpos.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

object KoinInitializer {
    fun init(extra: KoinAppDeclaration? = null): KoinApplication {
        Napier.base(DebugAntilog())
        return startKoin {
            extra?.invoke(this)
            modules(sharedModule, platformModule)
        }
    }
}
