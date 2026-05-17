package com.coditria.footpos.data.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsFactory {
    actual fun create(): Settings {
        // Use a named suite so the app's preferences are isolated from any host process defaults.
        val defaults = NSUserDefaults(suiteName = SUITE_NAME) ?: NSUserDefaults.standardUserDefaults
        return NSUserDefaultsSettings(defaults)
    }

    private companion object {
        const val SUITE_NAME = "com.coditria.footpos.settings"
    }
}
