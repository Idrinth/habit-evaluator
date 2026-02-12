package de.idrinth.habitevaluator.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FontSizeHelperTest {

    private Context baseContext;
    private SharedPreferences prefs;
    private Resources resources;
    private Context configContext;

    @Before
    public void setUp() {
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
    public void testSystemDefaultReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_SYSTEM);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    public void testNullSettingReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(null);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    public void testUnknownSettingReturnsOriginalContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn("UNKNOWN_VALUE");

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(baseContext, result);
        verify(baseContext, never()).createConfigurationContext(any());
    }

    @Test
    public void testXsFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_XS);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 0.8f));
    }

    @Test
    public void testSmallFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_SMALL);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 0.9f));
    }

    @Test
    public void testNormalFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_NORMAL);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 1.0f));
    }

    @Test
    public void testLargeFontSizeCreatesConfigurationContext() {
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_LARGE);

        Context result = FontSizeHelper.applyFontScale(baseContext);

        assertSame(configContext, result);
        verify(baseContext).createConfigurationContext(argThat(config -> config.fontScale == 1.2f));
    }

    @Test
    public void testCustomScaleDoesNotModifyOriginalConfiguration() {
        Configuration originalConfig = new Configuration();
        float originalFontScale = originalConfig.fontScale;
        when(resources.getConfiguration()).thenReturn(originalConfig);
        when(prefs.getString(SettingsActivity.KEY_FONT_SIZE, SettingsActivity.FONT_SIZE_SYSTEM))
                .thenReturn(SettingsActivity.FONT_SIZE_LARGE);

        FontSizeHelper.applyFontScale(baseContext);

        assertEquals(originalFontScale, originalConfig.fontScale, 0.001f);
    }
}
