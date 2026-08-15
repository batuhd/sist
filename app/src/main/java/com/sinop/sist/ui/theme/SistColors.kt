package com.sinop.sist.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Finansal anlam renkleri — TEK KAYNAK.
 * Kâr/zarar, gelir/gider, hero yüzeyi ve kategori vurguları buradan okunur.
 * Bildirimler ve widget'lar dahil hiçbir yerde hardcoded renk kullanılmamalı.
 */
@Immutable
data class SistColors(
    // Finansal anlam: kâr / gelir
    val positive: Color,
    val positiveContainer: Color,
    // Finansal anlam: zarar / gider
    val negative: Color,
    val negativeContainer: Color,
    // Uyarı
    val warning: Color,
    // İmza altın vurgu
    val gold: Color,
    val goldContainer: Color,
    // Hero yüzey (başlık paneli) — her iki temada da derin yeşil-siyah
    val hero: Color,
    val heroVariant: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    // Kategori vurguları
    val categoryYellow: Color,
    val categoryYellowContainer: Color,
    val categoryBlue: Color,
    val categoryBlueContainer: Color,
    val categoryPurple: Color,
    val categoryPurpleContainer: Color,
    val categoryOrange: Color,
    val categoryOrangeContainer: Color
)

val LightSistColors = SistColors(
    positive = Color(0xFF0E7A55),
    positiveContainer = Color(0xFFDDF5E9),
    negative = Color(0xFFC43B3B),
    negativeContainer = Color(0xFFFBE6E6),
    warning = Color(0xFFB45309),
    gold = Color(0xFFA97C0B),
    goldContainer = Color(0xFFFFF0CD),
    hero = HeroGreen,
    heroVariant = HeroGreenVariant,
    onHero = OnHeroGreen,
    onHeroMuted = HeroMutedGreen,
    categoryYellow = CardYellow,
    categoryYellowContainer = CardYellowLight,
    categoryBlue = CardBlue,
    categoryBlueContainer = CardBlueLight,
    categoryPurple = CardPurple,
    categoryPurpleContainer = CardPurpleLight,
    categoryOrange = CardOrange,
    categoryOrangeContainer = CardOrangeLight
)

val DarkSistColors = SistColors(
    positive = Color(0xFF4EDC9D),
    positiveContainer = Color(0xFF0C3A28),
    negative = Color(0xFFFF7B7B),
    negativeContainer = Color(0xFF431A1A),
    warning = Color(0xFFFBBF24),
    gold = Color(0xFFE8C15C),
    goldContainer = Color(0xFF46370A),
    hero = HeroGreen,
    heroVariant = HeroGreenVariant,
    onHero = OnHeroGreen,
    onHeroMuted = HeroMutedGreen,
    categoryYellow = Color(0xFFFACC15),
    categoryYellowContainer = Color(0xFF3B2F05),
    categoryBlue = Color(0xFF60A5FA),
    categoryBlueContainer = Color(0xFF10294A),
    categoryPurple = Color(0xFFC084FC),
    categoryPurpleContainer = Color(0xFF2E1445),
    categoryOrange = Color(0xFFFB923C),
    categoryOrangeContainer = Color(0xFF3F2008)
)

val LocalSistColors = staticCompositionLocalOf { LightSistColors }
