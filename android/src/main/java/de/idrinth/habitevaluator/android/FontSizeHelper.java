package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/**
 * Helper class for applying custom font size scaling throughout the app.
 * Call {@link #applyFontScale(Context)} in each Activity's attachBaseContext method.
 */
public final class FontSizeHelper {

    private FontSizeHelper() {
    }

    /**
     * Creates a new Context with the user's preferred font scale applied.
     * This should be called in each Activity's attachBaseContext method.
     * If the user has selected "System default", the original context is returned unchanged.
     *
     * @param baseContext the base context to wrap
     * @return a new context with the font scale applied, or the original if using system default
     */
    public static Context applyFontScale(Context baseContext) {
        SharedPreferences prefs = baseContext.getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String fontSizeSetting = prefs.getString(
                SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM);
        float scale = SettingsActivity.getFontScale(fontSizeSetting);

        // If scale is -1, use system default (don't override)
        if (scale < 0) {
            return baseContext;
        }

        Configuration config = new Configuration(baseContext.getResources().getConfiguration());
        config.fontScale = scale;

        return baseContext.createConfigurationContext(config);
    }
}
