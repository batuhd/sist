package com.sinop.sist.presentation.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sinop.sist.R
import com.sinop.sist.util.formatCurrency
import java.util.Locale

/**
 * Tum widget'larda ortak kullanilan tasarim kabugu.
 * Ana uygulamadaki hero kart diliyle birebir tutarli:
 * derin orman yesili zemin, altin vurgu, 28dp kose yaricapi.
 */
@Composable
fun SistWidgetContainer(
    openAppAction: Action,
    content: @Composable () -> Unit
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(28.dp)
            .background(SistWidgetColors.hero)
            .clickable(openAppAction)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        content()
    }
}

/**
 * Widget basligi: marka isareti + baslik + (istege bagli) calisan yenile butonu
 * ve ayarlar (dişli) butonu.
 */
@Composable
fun SistWidgetHeader(
    title: String,
    refreshAction: Action? = null,
    settingsAction: Action? = null
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_brand),
            contentDescription = null,
            modifier = GlanceModifier.size(20.dp)
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = title,
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        if (settingsAction != null) {
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .cornerRadius(14.dp)
                    .background(SistWidgetColors.heroVariant)
                    .clickable(settingsAction),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_settings),
                    contentDescription = "Ayarlar",
                    modifier = GlanceModifier.size(15.dp),
                    colorFilter = ColorFilter.tint(SistWidgetColors.onHeroMuted)
                )
            }
            Spacer(GlanceModifier.width(4.dp))
        }
        if (refreshAction != null) {
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .cornerRadius(14.dp)
                    .background(SistWidgetColors.heroVariant)
                    .clickable(refreshAction),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = "Yenile",
                    modifier = GlanceModifier.size(15.dp),
                    colorFilter = ColorFilter.tint(SistWidgetColors.onHeroMuted)
                )
            }
        }
    }
}

@Composable
fun WidgetAmount(text: String, fontSize: Int, color: ColorProvider) {
    Text(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        ),
        maxLines = 1
    )
}

/**
 * Arti/eksi isareti, yon oku ve renkle desteklenmis degisim rozeti.
 */
@Composable
fun WidgetChangeBadge(text: String, positive: Boolean, showIcon: Boolean = true) {
    val color = if (positive) SistWidgetColors.onHeroPositive else SistWidgetColors.onHeroNegative
    val container = if (positive) SistWidgetColors.onHeroPositiveContainer else SistWidgetColors.onHeroNegativeContainer

    Row(
        modifier = GlanceModifier
            .cornerRadius(10.dp)
            .background(container)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showIcon) {
            Image(
                provider = ImageProvider(
                    if (positive) R.drawable.ic_widget_arrow_up else R.drawable.ic_widget_arrow_down
                ),
                contentDescription = null,
                modifier = GlanceModifier.size(10.dp),
                colorFilter = ColorFilter.tint(color)
            )
            Spacer(GlanceModifier.width(2.dp))
        }
        Text(
            text = text,
            style = TextStyle(
                color = color,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
fun WidgetProgress(progress: Float, color: ColorProvider) {
    LinearProgressIndicator(
        progress.coerceIn(0f, 1f),
        GlanceModifier.fillMaxWidth().height(6.dp).cornerRadius(3.dp),
        color,
        SistWidgetColors.heroVariant
    )
}

@Composable
fun WidgetStaleChip() {
    Row(
        modifier = GlanceModifier
            .cornerRadius(10.dp)
            .background(SistWidgetColors.onHeroGoldContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_alert),
            contentDescription = null,
            modifier = GlanceModifier.size(10.dp),
            colorFilter = ColorFilter.tint(SistWidgetColors.onHeroWarning)
        )
        Spacer(GlanceModifier.width(3.dp))
        Text(
            text = "Güncel değil",
            style = TextStyle(
                color = SistWidgetColors.onHeroWarning,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun WidgetEmptyState(title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(18.dp)
                .background(SistWidgetColors.heroVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_brand),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = title,
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (15 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
}

@Composable
fun WidgetLegendDot(color: ColorProvider) {
    Box(
        modifier = GlanceModifier
            .size(8.dp)
            .cornerRadius(4.dp)
            .background(color)
    ) { }
}

/**
 * Buton gorunumlu tıklanabilir kapsul; bos durum yonlendirmeleri icin.
 */
@Composable
fun WidgetPillButton(
    label: String,
    action: Action
) {
    Box(
        modifier = GlanceModifier
            .cornerRadius(18.dp)
            .background(SistWidgetColors.onHeroGoldContainer)
            .clickable(action)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = SistWidgetColors.onHeroGold,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

fun widgetSignedCurrency(value: Double): String {
    val sign = if (value >= 0) "+" else ""
    return "$sign${value.formatCurrency()}"
}

fun widgetPercent(value: Double?): String {
    if (value == null) return ""
    val sign = if (value >= 0) "+" else ""
    return String.format(Locale.forLanguageTag("tr-TR"), "%s%.2f%%", sign, value)
}
