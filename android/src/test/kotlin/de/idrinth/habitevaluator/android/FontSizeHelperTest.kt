package de.idrinth.habitevaluator.android

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class FontSizeHelperTest {

    private lateinit var baseContext: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var resources: Resources
    private lateinit var configContext: Context

    @BeforeEach
    fun setUp() {
        baseContext = mock(Context::class.java)
        prefs = mock(SharedPreferences::class.java)
        resources = mock(Resources::class.java)
        configContext = mock(Context::class.java)

        `when`(baseContext.getSharedPreferences(SettingsConstants.PREFS_NAME, Context.MODE_PRIVATE))
            .thenReturn(prefs)
        `when`(baseContext.resources).thenReturn(resources)
        `when`(resources.configuration).thenReturn(Configuration())
        `when`(baseContext.createConfigurationContext(any(Configuration::class.java)))
            .thenReturn(configContext)
    }

    @Test
    fun testSystemDefaultReturnsOriginalContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_SYSTEM)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(baseContext, result)
        verify(baseContext, never()).createConfigurationContext(any())
    }

    @Test
    fun testNullSettingReturnsOriginalContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(null)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(baseContext, result)
        verify(baseContext, never()).createConfigurationContext(any())
    }

    @Test
    fun testUnknownSettingReturnsOriginalContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn("UNKNOWN_VALUE")

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(baseContext, result)
        verify(baseContext, never()).createConfigurationContext(any())
    }

    @Test
    fun testXsFontSizeCreatesConfigurationContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_XS)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(configContext, result)
        verify(baseContext).createConfigurationContext(argThat { config -> config.fontScale == 0.8f })
    }

    @Test
    fun testSmallFontSizeCreatesConfigurationContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_SMALL)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(configContext, result)
        verify(baseContext).createConfigurationContext(argThat { config -> config.fontScale == 0.9f })
    }

    @Test
    fun testNormalFontSizeCreatesConfigurationContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_NORMAL)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(configContext, result)
        verify(baseContext).createConfigurationContext(argThat { config -> config.fontScale == 1.0f })
    }

    @Test
    fun testLargeFontSizeCreatesConfigurationContext() {
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_LARGE)

        val result = FontSizeHelper.applyFontScale(baseContext)

        assertSame(configContext, result)
        verify(baseContext).createConfigurationContext(argThat { config -> config.fontScale == 1.2f })
    }

    @Test
    fun testCustomScaleDoesNotModifyOriginalConfiguration() {
        val originalConfig = Configuration()
        val originalFontScale = originalConfig.fontScale
        `when`(resources.configuration).thenReturn(originalConfig)
        `when`(prefs.getString(SettingsConstants.KEY_FONT_SIZE, SettingsConstants.FONT_SIZE_SYSTEM))
            .thenReturn(SettingsConstants.FONT_SIZE_LARGE)

        FontSizeHelper.applyFontScale(baseContext)

        assertEquals(originalFontScale, originalConfig.fontScale, 0.001f)
    }
}
