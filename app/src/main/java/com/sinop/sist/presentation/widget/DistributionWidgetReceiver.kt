package com.sinop.sist.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
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
import com.sinop.sist.SistApplication
import com.sinop.sist.util.formatCurrency
import java.util.Locale

class DistributionWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DistributionWidget()
}

class DistributionWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(300.dp, 170.dp),
            DpSize(300.dp, 300.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SistApplication
        val snapshot = if (app != null) {
            try {
                WidgetDataProvider.getDistributionSnapshot(app)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                    val widgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
                    val settingsAction = widgetSettingsAction(context, widgetId)
                SistWidgetContainer(openAppAction = actionRunCallback<OpenSistAppAction>()) {
                    if (snapshot != null && snapshot.parts.isNotEmpty()) {
                        DistributionWidgetContent(snapshot, settingsAction)
                    } else {
                        WidgetEmptyState(
                            title = "Portföyün boş",
                            subtitle = "Varlık eklemek için dokun"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionWidgetContent(snapshot: WidgetDataProvider.DistributionSnapshot, settingsAction: Action) {
    val size = LocalSize.current

    when {
        size.height <= 110.dp -> DistributionCompact(snapshot, settingsAction)
        size.height <= 170.dp -> DistributionExpanded(snapshot, settingsAction)
        else -> DistributionFull(snapshot, settingsAction)
    }
}

@Composable
private fun DistributionCompact(snapshot: WidgetDataProvider.DistributionSnapshot, settingsAction: Action) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföy Dağılımı", refreshAction = actionRunCallback<RefreshDistributionAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        StackedBar(snapshot)
        Spacer(GlanceModifier.height(6.dp))
        // Lejant: bar'daki TUM dilimleri her zaman kapsar (bar-lejant senkronu).
        Text(
            text = snapshot.parts.joinToString("   ") { part ->
                String.format(Locale.forLanguageTag("tr-TR"), "%s %%%.0f", part.label, part.share * 100)
            },
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )
    }
}

@Composable
private fun DistributionExpanded(snapshot: WidgetDataProvider.DistributionSnapshot, settingsAction: Action) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföy Dağılımı", refreshAction = actionRunCallback<RefreshDistributionAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        WidgetAmount(text = snapshot.totalValue.formatCurrency(), fontSize = 22, color = SistWidgetColors.onHero)
        if (snapshot.maxAssetShare >= 0.40) {
            Spacer(GlanceModifier.height(4.dp))
            ConcentrationChip(snapshot)
        }
        Spacer(GlanceModifier.height(8.dp))
        StackedBar(snapshot)
        Spacer(GlanceModifier.height(8.dp))
        Column {
            snapshot.parts.forEach { part ->
                LegendRow(part)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DistributionFull(snapshot: WidgetDataProvider.DistributionSnapshot, settingsAction: Action) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföy Dağılımı", refreshAction = actionRunCallback<RefreshDistributionAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))

        Column {
            WidgetAmount(text = snapshot.totalValue.formatCurrency(), fontSize = 26, color = SistWidgetColors.onHero)
            if (snapshot.maxAssetShare >= 0.40) {
                Spacer(GlanceModifier.height(4.dp))
                ConcentrationChip(snapshot)
            }
            Spacer(GlanceModifier.height(8.dp))
            Column {
                // Hangi fon, hangi hisse: varlik bazli satirlar.
                snapshot.assetRows.forEach { row ->
                    DistributionAssetRowContent(row)
                    Spacer(GlanceModifier.height(3.dp))
                }
            }
        }

        Spacer(GlanceModifier.height(12.dp))
        StackedBar(snapshot)
        Spacer(GlanceModifier.height(8.dp))
        Column {
            snapshot.parts.forEach { part ->
                LegendRow(part)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DistributionAssetRowContent(row: WidgetDataProvider.DistributionAssetRow) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        WidgetLegendDot(ColorProvider(WidgetDataProvider.assetTypeColorKey(row.assetType)))
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = row.symbol,
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            text = "${row.typeLabel} %${"%.0f".format(row.share * 100)}",
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = row.value.formatCurrency(),
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun StackedBar(snapshot: WidgetDataProvider.DistributionSnapshot) {
    val size = LocalSize.current
    val availableWidth = (size.width - 32.dp).coerceAtLeast(1.dp)

    // Son dilim kalan boslugu doldurur: cubuk her boyutta tam genislik kaplar.
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(10.dp)
            .cornerRadius(5.dp)
    ) {
        snapshot.parts.dropLast(1).forEach { part ->
            Box(
                modifier = GlanceModifier
                    .height(10.dp)
                    .width(availableWidth * part.share.coerceIn(0.0, 1.0).toFloat())
                    .background(ColorProvider(WidgetDataProvider.assetTypeColorKey(part.assetType)))
            ) { }
        }
        snapshot.parts.lastOrNull()?.let { part ->
            Box(
                modifier = GlanceModifier
                    .height(10.dp)
                    .defaultWeight()
                    .background(ColorProvider(WidgetDataProvider.assetTypeColorKey(part.assetType)))
            ) { }
        }
    }
}

@Composable
private fun LegendRow(part: WidgetDataProvider.DistributionPart) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        WidgetLegendDot(ColorProvider(WidgetDataProvider.assetTypeColorKey(part.assetType)))
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = part.label,
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = String.format(Locale.forLanguageTag("tr-TR"), "%%%.1f", part.share * 100),
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = part.value.formatCurrency(),
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ConcentrationChip(snapshot: WidgetDataProvider.DistributionSnapshot) {
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
            text = String.format(Locale.forLanguageTag("tr-TR"), "Tek varlık %%%.0f", snapshot.maxAssetShare * 100),
            style = TextStyle(
                color = SistWidgetColors.onHeroWarning,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
