package com.coditria.footpos.di

import android.content.Context
import org.koin.android.ext.koin.androidContext

object AndroidKoinInitializer {
    fun init(context: Context) {
        KoinInitializer.init { androidContext(context) }
    }
}
