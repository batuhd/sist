package com.sinop.sist.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.util.formatCurrency

class PortfolioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PortfolioWidget()
}

class PortfolioWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 50.dp),
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(300.dp, 170.dp),
            DpSize(300.dp, 300.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SistApplication
        val widgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
        val settingsAction = widgetSettingsAction(context, widgetId)
        val (summary, assets, movers, stale) = if (app != null) {
            try {
                val summary = WidgetDataProvider.getPortfolioSummary(app)
                val assets = WidgetDataProvider.getAssetsWithHoldings(app)
                val movers = WidgetDataProvider.getTopMovers(app, 5)
                Quadruple(summary, assets, movers, WidgetDataProvider.isStale(assets))
            } catch (_: Exception) {
                Quadruple(PortfolioSummary(0.0, null, null, null, emptyMap()), emptyList(), emptyList(), false)
            }
        } else {
            Quadruple(PortfolioSummary(0.0, null, null, null, emptyMap()), emptyList(), emptyList(), false)
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                SistWidgetContainer(openAppAction = actionRunCallback<OpenSistAppAction>()) {
                    PortfolioWidgetContent(summary = summary, assets = assets, movers = movers, stale = stale, settingsAction = settingsAction)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun PortfolioWidgetContent(
    summary: PortfolioSummary,
    assets: List<AssetWithPrice>,
    movers: List<AssetWithPrice>,
    stale: Boolean,
    settingsAction: Action
) {
    val size = LocalSize.current
    val totalValue = summary.totalValue ?: 0.0
    val profitLoss = summary.totalProfitLoss
    val profitPercent = summary.totalProfitLossPercent

    when {
        assets.isEmpty() -> {
            WidgetEmptyState(
                title = "Portföyün boş",
                subtitle = "Varlık eklemek için dokun"
            )
        }
        size.height <= 50.dp -> {
            PortfolioCompact(totalValue, profitLoss, profitPercent)
        }
        size.height <= 110.dp -> {
            PortfolioMedium(totalValue, profitLoss, profitPercent, movers, settingsAction)
        }
        size.height <= 170.dp -> {
            PortfolioExpanded(totalValue, profitLoss, profitPercent, movers, stale, settingsAction)
        }
        else -> {
            PortfolioFull(totalValue, profitLoss, profitPercent, assets, stale, settingsAction)
        }
    }
}

@Composable
private fun PortfolioCompact(totalValue: Double, profitLoss: Double?, profitPercent: Double?) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "Portföyüm",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (12 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
            WidgetAmount(text = totalValue.formatCurrency(), fontSize = 18, color = SistWidgetColors.onHero)
        }
        Spacer(GlanceModifier.width(8.dp))
        if (profitLoss != null) {
            WidgetChangeBadge(
                text = "${widgetSignedCurrency(profitLoss)} ${widgetPercent(profitPercent)}",
                positive = profitLoss >= 0,
                showIcon = true
            )
        }
    }
}

@Composable
private fun PortfolioMedium(
    totalValue: Double,
    profitLoss: Double?,
    profitPercent: Double?,
    movers: List<AssetWithPrice>,
    settingsAction: Action
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföyüm", refreshAction = actionRunCallback<RefreshPortfolioAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        WidgetAmount(text = totalValue.formatCurrency(), fontSize = 22, color = SistWidgetColors.onHero)
        if (profitLoss != null) {
            Spacer(GlanceModifier.height(2.dp))
            WidgetChangeBadge(
                text = "${widgetSignedCurrency(profitLoss)} ${widgetPercent(profitPercent)}",
                positive = profitLoss >= 0
            )
        }
        if (movers.isNotEmpty()) {
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = "Hareketliler",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Column {
                movers.take(2).forEach { asset ->
                    MoverRow(asset)
                    Spacer(GlanceModifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PortfolioExpanded(
    totalValue: Double,
    profitLoss: Double?,
    profitPercent: Double?,
    movers: List<AssetWithPrice>,
    stale: Boolean,
    settingsAction: Action
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföyüm", refreshAction = actionRunCallback<RefreshPortfolioAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            WidgetAmount(text = totalValue.formatCurrency(), fontSize = 24, color = SistWidgetColors.onHero)
            if (profitLoss != null) {
                Spacer(GlanceModifier.width(8.dp))
                WidgetChangeBadge(
                    text = "${widgetSignedCurrency(profitLoss)} ${widgetPercent(profitPercent)}",
                    positive = profitLoss >= 0
                )
            }
            if (stale) {
                Spacer(GlanceModifier.width(6.dp))
                WidgetStaleChip()
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = "Hareketliler (alıştan bu yana)",
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(4.dp))
        Column {
            movers.take(4).forEach { asset ->
                MoverRow(asset)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun PortfolioFull(
    totalValue: Double,
    profitLoss: Double?,
    profitPercent: Double?,
    assets: List<AssetWithPrice>,
    stale: Boolean,
    settingsAction: Action
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Portföyüm", refreshAction = actionRunCallback<RefreshPortfolioAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            WidgetAmount(text = totalValue.formatCurrency(), fontSize = 26, color = SistWidgetColors.onHero)
            if (profitLoss != null) {
                Spacer(GlanceModifier.width(8.dp))
                WidgetChangeBadge(
                    text = "${widgetSignedCurrency(profitLoss)} ${widgetPercent(profitPercent)}",
                    positive = profitLoss >= 0
                )
            }
            if (stale) {
                Spacer(GlanceModifier.width(6.dp))
                WidgetStaleChip()
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        LazyColumn(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        ) {
            items(assets, itemId = { it.asset.id }) { asset ->
                AssetRowDetailed(asset)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun MoverRow(asset: AssetWithPrice) {
    val profitLoss = asset.profitLoss ?: 0.0
    val size = LocalSize.current
    val wide = size.width > 200.dp
    val badgeText = if (wide) {
        "${widgetSignedCurrency(profitLoss)} (${widgetPercent(asset.profitLossPercent)})"
    } else {
        widgetPercent(asset.profitLossPercent)
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = asset.asset.symbol,
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = (asset.currentValue ?: 0.0).formatCurrency(),
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.width(6.dp))
        WidgetChangeBadge(
            text = badgeText,
            positive = profitLoss >= 0,
            showIcon = false
        )
    }
}

@Composable
private fun AssetRowDetailed(asset: AssetWithPrice) {
    val profitLoss = asset.profitLoss ?: 0.0
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionRunCallback<OpenSistAppAction>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = asset.asset.symbol,
                style = TextStyle(
                    color = SistWidgetColors.onHero,
                    fontSize = (13 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(
                text = (asset.currentValue ?: 0.0).formatCurrency(),
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        Spacer(GlanceModifier.width(6.dp))
        WidgetChangeBadge(
            text = "${widgetSignedCurrency(profitLoss)} (${widgetPercent(asset.profitLossPercent)})",
            positive = profitLoss >= 0,
            showIcon = true
        )
    }
}
