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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sinop.sist.SistApplication
import com.sinop.sist.util.formatCurrency

class NetWorthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NetWorthWidget()
}

class NetWorthWidget : GlanceAppWidget() {

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
        val breakdown = if (app != null) {
            try {
                WidgetDataProvider.getNetWorthBreakdown(app)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                SistWidgetContainer(openAppAction = actionRunCallback<OpenSistAppAction>()) {
                    if (breakdown != null) {
                        NetWorthWidgetContent(breakdown, settingsAction)
                    } else {
                        WidgetEmptyState(
                            title = "Veri yok",
                            subtitle = "Hesap eklemek için dokun"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetWorthWidgetContent(
    breakdown: WidgetDataProvider.NetWorthBreakdown,
    settingsAction: Action
) {
    val size = LocalSize.current

    when {
        size.height <= 50.dp -> NetWorthCompact(breakdown.total)
        size.height <= 110.dp -> NetWorthMedium(breakdown, settingsAction)
        else -> NetWorthFull(breakdown, settingsAction)
    }
}

@Composable
private fun NetWorthCompact(total: Double) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "Toplam Varlık",
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        WidgetAmount(text = total.formatCurrency(), fontSize = 18, color = SistWidgetColors.onHero)
    }
}

@Composable
private fun NetWorthMedium(breakdown: WidgetDataProvider.NetWorthBreakdown, settingsAction: Action) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Toplam Varlık", refreshAction = actionRunCallback<RefreshPortfolioAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        WidgetAmount(text = breakdown.total.formatCurrency(), fontSize = 22, color = SistWidgetColors.onHero)
        Spacer(GlanceModifier.height(6.dp))
        Column {
            BreakdownRow(label = "Portföy", value = breakdown.portfolio)
            Spacer(GlanceModifier.height(2.dp))
            BreakdownRow(label = "Hesaplar", value = breakdown.accounts)
            breakdown.accountList.take(2).forEach { account ->
                AccountRow(account)
                Spacer(GlanceModifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun NetWorthFull(breakdown: WidgetDataProvider.NetWorthBreakdown, settingsAction: Action) {
    val total = breakdown.total.coerceAtLeast(0.01)
    val accountShare = (breakdown.accounts / total).coerceIn(0.0, 1.0)
    val portfolioShare = (breakdown.portfolio / total).coerceIn(0.0, 1.0)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Toplam Varlık", refreshAction = actionRunCallback<RefreshPortfolioAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        WidgetAmount(text = breakdown.total.formatCurrency(), fontSize = 26, color = SistWidgetColors.onHero)

        Spacer(GlanceModifier.height(8.dp))
        Column {
            BreakdownRow(label = "Portföy", value = breakdown.portfolio)
            Spacer(GlanceModifier.height(3.dp))
            BreakdownRow(label = "Hesaplar", value = breakdown.accounts)
            Column {
                // Hesaplar grubunun alt kalemleri: girintili ve soluk tonda.
                breakdown.accountList.take(5).forEach { account ->
                    AccountRow(account)
                    Spacer(GlanceModifier.height(1.dp))
                }
            }
        }

        Spacer(GlanceModifier.height(12.dp))
        ShareBar(accountShare = accountShare, portfolioShare = portfolioShare)

        Spacer(GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            WidgetLegendDot(SistWidgetColors.onHeroPositive)
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = "Portföy %${"%.0f".format(portfolioShare * 100)}",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            WidgetLegendDot(SistWidgetColors.onHeroGold)
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = "Hesaplar %${"%.0f".format(accountShare * 100)}",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun AccountRow(account: com.sinop.sist.domain.model.Account) {
    // Alt kalem: girintili, daha kucuk ve soluk tonda.
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        WidgetLegendDot(SistWidgetColors.onHeroMuted)
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = account.name,
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            text = account.balance.formatCurrency(),
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun BreakdownRow(label: String, value: Double) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        WidgetLegendDot(
            if (label == "Hesaplar") SistWidgetColors.onHeroGold else SistWidgetColors.onHeroPositive
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = label,
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = value.formatCurrency(),
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ShareBar(accountShare: Double, portfolioShare: Double) {
    val size = LocalSize.current
    val availableWidth = (size.width - 32.dp).coerceAtLeast(1.dp)

    // Zemin (hesaplar) + ucta portfoy dilimi: toplam genislik asla tasmaz.
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(8.dp)
            .cornerRadius(4.dp)
            .background(SistWidgetColors.onHeroGold),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (portfolioShare > 0) {
            Box(
                modifier = GlanceModifier
                    .height(8.dp)
                    .cornerRadius(4.dp)
                    .background(SistWidgetColors.onHeroPositive)
                    .width((availableWidth * portfolioShare.toFloat()).coerceAtMost(availableWidth))
            ) { }
        }
    }
}
