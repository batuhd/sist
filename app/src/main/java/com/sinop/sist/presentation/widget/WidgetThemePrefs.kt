package com.sinop.sist.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.AppWidgetId

/**
 * Widget renk temasi tercihleri.
 * Her widget kendi temasini hatirlar; varsayilan "marka" (orman yesili).
 */
object WidgetThemePrefs {

    const val THEME_BRAND = "brand"
    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"
    const val THEME_SYSTEM = "system"

    const val FONT_NORMAL = "font_normal"
    const val FONT_BIG = "font_big"
    const val FONT_HUGE = "font_huge"

    private const val PREFS_NAME = "sist_widget_prefs"
    private const val KEY_PREFIX = "theme_"
    private const val FONT_KEY_PREFIX = "font_"

    fun themeFor(context: Context, widgetId: Int): String {
        if (widgetId < 0) return THEME_BRAND
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PREFIX + widgetId, THEME_BRAND) ?: THEME_BRAND
    }

    fun themeFor(context: Context, glanceId: GlanceId): String {
        val widgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: -1
        return themeFor(context, widgetId)
    }

    fun saveTheme(context: Context, widgetId: Int, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PREFIX + widgetId, theme)
            .apply()
    }

    fun fontScaleFor(context: Context, widgetId: Int): Float {
        return fontScaleFor(fontKeyFor(context, widgetId))
    }

    fun fontKeyFor(context: Context, widgetId: Int): String {
        if (widgetId < 0) return FONT_NORMAL
        val key = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(FONT_KEY_PREFIX + widgetId, FONT_NORMAL) ?: FONT_NORMAL
        return key
    }

    fun fontScaleFor(context: Context, glanceId: GlanceId): Float {
        val widgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: -1
        return fontScaleFor(context, widgetId)
    }

    fun fontScaleFor(fontKey: String): Float = when (fontKey) {
        FONT_BIG -> 1.15f
        FONT_HUGE -> 1.3f
        else -> 1.0f
    }

    fun saveFontScale(context: Context, widgetId: Int, fontKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(FONT_KEY_PREFIX + widgetId, fontKey)
            .apply()
    }

    fun labelForFont(fontKey: String): String = when (fontKey) {
        FONT_BIG -> "Büyük"
        FONT_HUGE -> "Çok büyük"
        else -> "Normal"
    }

    fun paletteFor(theme: String): WidgetPalette = when (theme) {
        THEME_DARK -> DarkWidgetPalette
        THEME_LIGHT -> LightWidgetPalette
        THEME_SYSTEM -> SystemWidgetPalette
        else -> BrandWidgetPalette
    }

    fun labelFor(theme: String): String = when (theme) {
        THEME_DARK -> "Koyu"
        THEME_LIGHT -> "Açık"
        THEME_SYSTEM -> "Sistem teması"
        else -> "Koyu Yeşil (marka)"
    }
}
