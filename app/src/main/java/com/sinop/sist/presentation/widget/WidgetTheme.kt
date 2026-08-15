package com.sinop.sist.presentation.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProviders
import androidx.glance.color.ColorProvider as DayNightColorProvider
import androidx.glance.color.colorProviders
import androidx.glance.unit.ColorProvider
import com.sinop.sist.R

/**
 * Widget paleti: ana uygulamanin SistColors token'lariyla birebir eslenir.
 * Degerler tek noktada (asagida) tanimlanir; sistem temasini izleyen palet
 * disindakiler kullanici secimiyle zorlanabilir (marka/koyu/acik).
 */
@Immutable
data class WidgetPalette(
    val hero: ColorProvider,
    val heroVariant: ColorProvider,
    val onHero: ColorProvider,
    val onHeroMuted: ColorProvider,
    val onHeroPositive: ColorProvider,
    val onHeroPositiveContainer: ColorProvider,
    val onHeroNegative: ColorProvider,
    val onHeroNegativeContainer: ColorProvider,
    val onHeroWarning: ColorProvider,
    val onHeroGold: ColorProvider,
    val onHeroGoldContainer: ColorProvider,
    val categoryYellow: ColorProvider,
    val categoryBlue: ColorProvider,
    val categoryPurple: ColorProvider,
    val categoryOrange: ColorProvider
)

// ---------------------------------------------------------------------
// Palet hex degerleri: SistColors.kt ile 1:1 eslesir.
// - "marka": Hero yesili (uygulama kimligi), tema bagimsiz
// - "koyu": DarkSistColors degerleri
// - "acik": LightSistColors degerleri
// - "sistem": aydinlikta acik, karanlikta koyu
// ---------------------------------------------------------------------
private fun cp(light: Long, dark: Long): ColorProvider =
    DayNightColorProvider(Color(light), Color(dark))

private fun fixed(hex: Long): ColorProvider = cp(hex, hex)

val BrandWidgetPalette = WidgetPalette(
    hero = fixed(0xFF06281C),
    heroVariant = fixed(0xFF0B3A2A),
    onHero = fixed(0xFFEDFBF4),
    onHeroMuted = fixed(0xFFA9D9C6),
    onHeroPositive = fixed(0xFF4EDC9D),
    onHeroPositiveContainer = fixed(0xFF0C3A28),
    onHeroNegative = fixed(0xFFFF7B7B),
    onHeroNegativeContainer = fixed(0xFF431A1A),
    onHeroWarning = fixed(0xFFFBBF24),
    onHeroGold = fixed(0xFFE8C15C),
    onHeroGoldContainer = fixed(0xFF46370A),
    categoryYellow = cp(0xFFEAB308, 0xFFFACC15),
    categoryBlue = cp(0xFF2F80ED, 0xFF60A5FA),
    categoryPurple = cp(0xFF9B51E0, 0xFFC084FC),
    categoryOrange = cp(0xFFF2994A, 0xFFFB923C)
)

val DarkWidgetPalette = WidgetPalette(
    hero = fixed(0xFF0F1512),
    heroVariant = fixed(0xFF2C322F),
    onHero = fixed(0xFFF6FBF8),
    onHeroMuted = fixed(0xFFBFC9C3),
    onHeroPositive = fixed(0xFF4EDC9D),
    onHeroPositiveContainer = fixed(0xFF0C3A28),
    onHeroNegative = fixed(0xFFFF7B7B),
    onHeroNegativeContainer = fixed(0xFF431A1A),
    onHeroWarning = fixed(0xFFFBBF24),
    onHeroGold = fixed(0xFFE8C15C),
    onHeroGoldContainer = fixed(0xFF46370A),
    categoryYellow = fixed(0xFFFACC15),
    categoryBlue = fixed(0xFF60A5FA),
    categoryPurple = fixed(0xFFC084FC),
    categoryOrange = fixed(0xFFFB923C)
)

val LightWidgetPalette = WidgetPalette(
    hero = fixed(0xFFF6FBF8),
    heroVariant = fixed(0xFFDBE5DF),
    onHero = fixed(0xFF171D1A),
    onHeroMuted = fixed(0xFF6F7974),
    onHeroPositive = fixed(0xFF0E7A55),
    onHeroPositiveContainer = fixed(0xFFDDF5E9),
    onHeroNegative = fixed(0xFFC43B3B),
    onHeroNegativeContainer = fixed(0xFFFBE6E6),
    onHeroWarning = fixed(0xFFB45309),
    onHeroGold = fixed(0xFFA97C0B),
    onHeroGoldContainer = fixed(0xFFFFF0CD),
    categoryYellow = fixed(0xFFEAB308),
    categoryBlue = fixed(0xFF2F80ED),
    categoryPurple = fixed(0xFF9B51E0),
    categoryOrange = fixed(0xFFF2994A)
)

val SystemWidgetPalette = WidgetPalette(
    hero = cp(0xFF06281C, 0xFF06281C),
    heroVariant = cp(0xFF0B3A2A, 0xFF0B3A2A),
    onHero = cp(0xFFEDFBF4, 0xFFEDFBF4),
    onHeroMuted = cp(0xFFA9D9C6, 0xFFA9D9C6),
    onHeroPositive = cp(0xFF4EDC9D, 0xFF4EDC9D),
    onHeroPositiveContainer = cp(0xFF0C3A28, 0xFF0C3A28),
    onHeroNegative = cp(0xFFFF7B7B, 0xFFFF7B7B),
    onHeroNegativeContainer = cp(0xFF431A1A, 0xFF431A1A),
    onHeroWarning = cp(0xFFFBBF24, 0xFFFBBF24),
    onHeroGold = cp(0xFFE8C15C, 0xFFE8C15C),
    onHeroGoldContainer = cp(0xFF46370A, 0xFF46370A),
    categoryYellow = cp(0xFFEAB308, 0xFFFACC15),
    categoryBlue = cp(0xFF2F80ED, 0xFF60A5FA),
    categoryPurple = cp(0xFF9B51E0, 0xFFC084FC),
    categoryOrange = cp(0xFFF2994A, 0xFFFB923C)
)

val LocalWidgetPalette = staticCompositionLocalOf { BrandWidgetPalette }

/**
 * Kullanici tarafindan secilebilen yazi boyutu olcegi.
 */
val LocalWidgetFontScale = staticCompositionLocalOf { 1.0f }

@Composable
fun SistWidgetTheme(
    palette: WidgetPalette = BrandWidgetPalette,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalWidgetPalette provides palette,
        LocalWidgetFontScale provides fontScale
    ) {
        GlanceTheme(colors = systemGlanceColors()) {
            content()
        }
    }
}

/**
 * Glance'in kendi bilesenleri (Button, CheckBox vb.) icin sistem temasi.
 * Palet seciminden bagimsiz olarak kaynak (day/night) renklerini kullanir.
 */
private fun systemGlanceColors(): ColorProviders = colorProviders(
    primary = ColorProvider(R.color.widget_positive),
    onPrimary = ColorProvider(R.color.widget_surface),
    primaryContainer = ColorProvider(R.color.widget_positive_container),
    onPrimaryContainer = ColorProvider(R.color.widget_text_primary),
    secondary = ColorProvider(R.color.widget_text_secondary),
    onSecondary = ColorProvider(R.color.widget_background),
    secondaryContainer = ColorProvider(R.color.widget_surface_variant),
    onSecondaryContainer = ColorProvider(R.color.widget_text_primary),
    tertiary = ColorProvider(R.color.widget_gold),
    onTertiary = ColorProvider(R.color.widget_background),
    tertiaryContainer = ColorProvider(R.color.widget_gold_container),
    onTertiaryContainer = ColorProvider(R.color.widget_text_primary),
    error = ColorProvider(R.color.widget_negative),
    onError = ColorProvider(R.color.widget_background),
    errorContainer = ColorProvider(R.color.widget_negative_container),
    onErrorContainer = ColorProvider(R.color.widget_text_primary),
    background = ColorProvider(R.color.widget_background),
    onBackground = ColorProvider(R.color.widget_text_primary),
    surface = ColorProvider(R.color.widget_surface),
    onSurface = ColorProvider(R.color.widget_text_primary),
    surfaceVariant = ColorProvider(R.color.widget_surface_variant),
    onSurfaceVariant = ColorProvider(R.color.widget_text_secondary),
    outline = ColorProvider(R.color.widget_outline),
    inverseOnSurface = ColorProvider(R.color.widget_on_hero),
    inverseSurface = ColorProvider(R.color.widget_hero),
    inversePrimary = ColorProvider(R.color.widget_on_hero_positive),
    widgetBackground = ColorProvider(R.color.widget_hero)
)

/**
 * Mevcut kodun SistWidgetColors.* referanslari bu nesne uzerinden
 * secili palete yonlendirilir.
 */
object SistWidgetColors {
    val current: WidgetPalette
        @Composable get() = LocalWidgetPalette.current

    val hero: ColorProvider @Composable get() = current.hero
    val heroVariant: ColorProvider @Composable get() = current.heroVariant
    val onHero: ColorProvider @Composable get() = current.onHero
    val onHeroMuted: ColorProvider @Composable get() = current.onHeroMuted
    val onHeroPositive: ColorProvider @Composable get() = current.onHeroPositive
    val onHeroPositiveContainer: ColorProvider @Composable get() = current.onHeroPositiveContainer
    val onHeroNegative: ColorProvider @Composable get() = current.onHeroNegative
    val onHeroNegativeContainer: ColorProvider @Composable get() = current.onHeroNegativeContainer
    val onHeroWarning: ColorProvider @Composable get() = current.onHeroWarning
    val onHeroGold: ColorProvider @Composable get() = current.onHeroGold
    val onHeroGoldContainer: ColorProvider @Composable get() = current.onHeroGoldContainer
    val categoryYellow: ColorProvider @Composable get() = current.categoryYellow
    val categoryBlue: ColorProvider @Composable get() = current.categoryBlue
    val categoryPurple: ColorProvider @Composable get() = current.categoryPurple
    val categoryOrange: ColorProvider @Composable get() = current.categoryOrange
}

/**
 * Font boyutu olcegi: tum widget metinlerinde (N * SistWidgetFonts.scale).sp kullanilir.
 */
object SistWidgetFonts {
    val scale: Float
        @Composable get() = LocalWidgetFontScale.current
}
