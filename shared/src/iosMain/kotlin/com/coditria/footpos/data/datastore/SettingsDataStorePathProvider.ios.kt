package com.coditria.footpos.data.datastore

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual class SettingsDataStorePathProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual fun directoryPath(): String {
        val documentsUrl: NSURL = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: error("Unable to resolve iOS documents directory for DataStore")
        return requireNotNull(documentsUrl.path) { "Documents URL has no path" }
    }
}
