package com.coditria.footpos

import android.app.Application
import com.coditria.footpos.di.AndroidKoinInitializer

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidKoinInitializer.init(this)
    }
}
