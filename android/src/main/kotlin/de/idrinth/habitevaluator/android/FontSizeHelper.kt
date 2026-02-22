package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.res.Configuration

object FontSizeHelper {

    fun applyFontScale(baseContext: Context): Context {
        val prefs = baseContext.getSharedPreferences(
            SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE
        )
        val fontSizeSetting = prefs.getString(
            SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM
        )
        val scale = SettingsConstants.getFontScale(fontSizeSetting)
        if (scale < 0) return baseContext

        val config = Configuration(baseContext.resources.configuration)
        config.fontScale = scale
        return baseContext.createConfigurationContext(config)
    }
}
