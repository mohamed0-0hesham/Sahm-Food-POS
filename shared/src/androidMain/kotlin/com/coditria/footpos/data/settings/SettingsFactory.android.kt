package com.coditria.footpos.data.settings

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class SettingsFactory(private val context: Context) {
    actual fun create(): Settings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return SharedPreferencesSettings(prefs)
    }

    private companion object {
        const val PREFS_NAME = "sahm_pos_settings"
    }
}
