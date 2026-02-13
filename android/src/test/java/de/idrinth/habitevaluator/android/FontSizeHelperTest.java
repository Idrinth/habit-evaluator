package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FontSizeHelperTest {

    private Context baseContext;
    private SharedPreferences prefs;
    private Resources resources;
    private Context configContext;

    @BeforeEach
    void setUp() {
        baseContext = mock(Context.class);
        prefs = mock(SharedPreferences.class);
        resources = mock(Resources.class);
        configContext = mock(Context.class);

        when(baseContext.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE))
                .thenReturn(prefs);
        when(baseContext.getResources()).thenReturn(resources);
        when(resources.getConfiguration()).thenReturn(new Configuration());
        when(baseContext.createConfigurationContext(any(Configuration.class)))
                .thenReturn(configContext);
    }

    @Test
    void testSystemDefaultReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_SYSTEM);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    void testNullSettingReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(null);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    void testUnknownSettingReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn("UNKNOWN_VALUE");

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    void testXsFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_XS);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 0.8f));
    }

    @Test
    void testSmallFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_SMALL);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 0.9f));
    }

    @Test
    void testNormalFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_NORMAL);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 1.0f));
    }

    @Test
    void testLargeFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_LARGE);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 1.2f));
    }

    @Test
    void testCustomScaleDoesNotModifyOriginalConfiguration() {
        Configuration originalConfig = new Configuration();
        float originalFontScale = originalConfig.fontScale;
        when(resources.getConfiguration()).thenReturn(originalConfig);
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_LARGE);

        FontSizeHelper.applyFontScale(baseContext);

        assertEquals(originalFontScale, originalConfig.fontScale, 0.001f);
    }
}
