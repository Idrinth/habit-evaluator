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
     *
     * @param baseContext the base context to wrap
     * @return a new context with the font scale applied
     */
    public static Context applyFontScale(Context baseContext) {
        SharedPreferences prefs = baseContext.getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String fontSizeSetting = prefs.getString(
                SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_NORMAL);
        float scale = SettingsActivity.getFontScale(fontSizeSetting);

        Configuration config = new Configuration(baseContext.getResources().getConfiguration());
        config.fontScale = scale;

        return baseContext.createConfigurationContext(config);
    }
}
