package com.coditria.footpos.data.datastore

import android.content.Context

actual class SettingsDataStorePathProvider(private val context: Context) {
    actual fun directoryPath(): String = context.filesDir.absolutePath
}
