package com.sinop.sist.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
// Sist Design Tokens — Bold Identity
// Derin orman yeşili primary + altın (gold) vurgu + "hero" yüzeyi.
// Legacy isimler (IncomeGreen vs.) geriye dönük uyumluluk için alias
// olarak korunur; yeni kod SistColors.kt'yi kullanmalıdır.
// =====================================================================

// --- Primary palette — Derin orman yeşili (forest emerald) ---
val Primary10 = Color(0xFF002117)
val Primary20 = Color(0xFF00382A)
val Primary30 = Color(0xFF004F3E)
val Primary40 = Color(0xFF00634D)
val Primary50 = Color(0xFF007B60)
val Primary60 = Color(0xFF009574)
val Primary80 = Color(0xFF5BDBAF)
val Primary90 = Color(0xFF7CF8C9)
val Primary100 = Color(0xFFFFFFFF)

// --- Secondary palette — Adaçayı yeşil-gri (sage) ---
val Secondary10 = Color(0xFF082019)
val Secondary20 = Color(0xFF1E352D)
val Secondary30 = Color(0xFF344B43)
val Secondary40 = Color(0xFF4C635A)
val Secondary80 = Color(0xFFB2CCC0)
val Secondary90 = Color(0xFFCEE9DC)

// --- Tertiary palette — Altın (gold, imza vurgu rengi) ---
val Tertiary10 = Color(0xFF261A00)
val Tertiary20 = Color(0xFF3E2E00)
val Tertiary30 = Color(0xFF594400)
val Tertiary40 = Color(0xFF7A5C0C)
val Tertiary80 = Color(0xFFEBC15F)
val Tertiary90 = Color(0xFFFFDF9E)

// --- Error ---
val Error10 = Color(0xFF410002)
val Error20 = Color(0xFF690005)
val Error30 = Color(0xFF93000A)
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val Error90 = Color(0xFFFFDAD6)

// --- Neutral — yeşil tonlu gri skala ---
val Neutral98 = Color(0xFFF6FBF8)
val Neutral95 = Color(0xFFEAF2ED)
val Neutral90 = Color(0xFFDBE5DF)
val Neutral80 = Color(0xFFBFC9C3)
val Neutral40 = Color(0xFF6F7974)
val Neutral30 = Color(0xFF3F4945)
val Neutral20 = Color(0xFF2C322F)
val Neutral10 = Color(0xFF171D1A)
val Neutral0 = Color(0xFF000000)

// --- Hero yüzey — her iki temada da derin yeşil-siyah ---
val HeroGreen = Color(0xFF06281C)
val HeroGreenVariant = Color(0xFF0B3A2A)
val OnHeroGreen = Color(0xFFEDFBF4)
val HeroMutedGreen = Color(0xFFA9D9C6)

// --- Yüzey varyantları ---
val SurfaceLight = Neutral98
val SurfaceDark = Color(0xFF0F1512)
val SurfaceVariantLight = Neutral90
val SurfaceVariantDark = Neutral30

// --- Kategori vurgu paleti (merkezi) ---
val CardYellow = Color(0xFFEAB308)
val CardYellowLight = Color(0xFFFEF3C7)
val CardBlue = Color(0xFF2F80ED)
val CardBlueLight = Color(0xFFE1EDFE)
val CardPurple = Color(0xFF9B51E0)
val CardPurpleLight = Color(0xFFF2E8FC)
val CardOrange = Color(0xFFF2994A)
val CardOrangeLight = Color(0xFFFDEEE0)

// =====================================================================
// Legacy alias'lar — geriye dönük uyumluluk.
// Yeni kod SistColors.kt'deki semantik token'ları kullanmalıdır.
// =====================================================================
@Deprecated("SistColors.positive kullanın")
val IncomeGreen = Color(0xFF0E7A55)

@Deprecated("SistColors.positiveContainer kullanın")
val IncomeGreenLight = Color(0xFFDDF5E9)

@Deprecated("SistColors.negative kullanın")
val ExpenseRed = Color(0xFFC43B3B)

@Deprecated("SistColors.negativeContainer kullanın")
val ExpenseRedLight = Color(0xFFFBE6E6)

@Deprecated("SistColors.warning kullanın")
val WarningAmber = Color(0xFFB45309)
