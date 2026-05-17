package com.coditria.footpos.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path
import okio.Path.Companion.toPath

/**
 * Builds the multiplatform DataStore that backs SettingsRepositoryImpl.
 * The directory differs per platform (filesDir on Android, Documents on iOS), so each
 * `actual` provides the host directory and the factory composes the filename.
 */
expect class SettingsDataStorePathProvider {
    fun directoryPath(): String
}

object SettingsDataStoreFactory {
    private const val FILE_NAME = "sahm_pos_settings.preferences_pb"

    fun create(pathProvider: SettingsDataStorePathProvider): DataStore<Preferences> {
        val path: Path = (pathProvider.directoryPath() + "/" + FILE_NAME).toPath()
        return PreferenceDataStoreFactory.createWithPath(produceFile = { path })
    }
}
