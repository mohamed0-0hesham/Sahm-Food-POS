package com.coditria.footpos.data.settings

import com.russhwolf.settings.Settings

/**
 * Builds the multiplatform [Settings] instance that backs SettingsRepositoryImpl.
 * Each platform constructs the appropriate concrete Settings (SharedPreferences on
 * Android, NSUserDefaults on iOS) so the data layer stays platform-neutral.
 */
expect class SettingsFactory {
    fun create(): Settings
}
