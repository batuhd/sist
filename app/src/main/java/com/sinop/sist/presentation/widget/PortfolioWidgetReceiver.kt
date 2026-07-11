package com.sinop.sist.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sinop.sist.R
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.first
import java.text.DecimalFormat

class PortfolioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PortfolioWidget()
}

class PortfolioWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 50.dp),
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(300.dp, 200.dp),
            DpSize(300.dp, 300.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SistApplication
        val (summary, assets) = if (app != null) {
            try {
                val summary = WidgetDataProvider.getPortfolioSummary(app)
                val assets = WidgetDataProvider.getAssetsWithHoldings(app)
                summary to assets
            } catch (_: Exception) {
                PortfolioSummary(0.0, null, null, null, emptyMap()) to emptyList()
            }
        } else {
            PortfolioSummary(0.0, null, null, null, emptyMap()) to emptyList()
        }

        provideContent {
            PortfolioWidgetContent(summary = summary, assets = assets)
        }
    }
}

@Composable
private fun PortfolioWidgetContent(summary: PortfolioSummary, assets: List<AssetWithPrice>) {
    val size = LocalSize.current
    val totalValue = summary.totalValue ?: 0.0
    val profitLoss = summary.totalProfitLoss

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(R.color.widget_background))
            .clickable(actionRunCallback<OpenAppAction>())
            .padding(16.dp)
    ) {
        when {
            size.width <= 180.dp && size.height <= 50.dp -> {
                PortfolioCompactContent(totalValue = totalValue, profitLoss = profitLoss)
            }
            size.height <= 110.dp -> {
                PortfolioMediumContent(
                    totalValue = totalValue,
                    profitLoss = profitLoss,
                    assets = assets
                )
            }
            else -> {
                PortfolioExpandedContent(
                    totalValue = totalValue,
                    profitLoss = profitLoss,
                    assets = assets
                )
            }
        }
    }
}

@Composable
private fun PortfolioCompactContent(totalValue: Double, profitLoss: Double?) {
    val isPositive = profitLoss != null && profitLoss >= 0
    val profitColorRes = if (isPositive) R.color.widget_profit else R.color.widget_loss
    val sign = if (isPositive) "+" else ""

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "Portföyüm",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = totalValue.formatCurrency(),
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        if (profitLoss != null) {
            Text(
                text = "$sign${profitLoss.formatCurrency()}",
                style = TextStyle(
                    color = ColorProvider(profitColorRes),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun PortfolioMediumContent(
    totalValue: Double,
    profitLoss: Double?,
    assets: List<AssetWithPrice>
) {
    val isPositive = profitLoss != null && profitLoss >= 0
    val profitColorRes = if (isPositive) R.color.widget_profit else R.color.widget_loss
    val sign = if (isPositive) "+" else ""
    val percentText = summaryPercent(profitLoss, totalValue)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        HeaderRow(title = "Portföyüm")

        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = totalValue.formatCurrency(),
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_primary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        if (profitLoss != null) {
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "$sign${profitLoss.formatCurrency()}$percentText",
                style = TextStyle(
                    color = ColorProvider(profitColorRes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (assets.isNotEmpty()) {
            Spacer(GlanceModifier.height(6.dp))
            assets.take(3).forEach { asset ->
                AssetRowCompact(asset = asset)
                Spacer(GlanceModifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun PortfolioExpandedContent(
    totalValue: Double,
    profitLoss: Double?,
    assets: List<AssetWithPrice>
) {
    val isPositive = profitLoss != null && profitLoss >= 0
    val profitColorRes = if (isPositive) R.color.widget_profit else R.color.widget_loss
    val sign = if (isPositive) "+" else ""
    val percentText = summaryPercent(profitLoss, totalValue)

    Column(modifier = GlanceModifier.fillMaxSize()) {
        HeaderRow(title = "Portföyüm")

        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = totalValue.formatCurrency(),
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_primary),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        )

        if (profitLoss != null) {
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "$sign${profitLoss.formatCurrency()}$percentText",
                style = TextStyle(
                    color = ColorProvider(profitColorRes),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (assets.isNotEmpty()) {
            Spacer(GlanceModifier.height(12.dp))
            Text(
                text = "Varlıklarım (${assets.size})",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            assets.forEach { asset ->
                AssetRowDetailed(asset = asset)
                Spacer(GlanceModifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun HeaderRow(title: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_secondary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(ColorProvider(R.color.widget_text_secondary))
                .padding(4.dp)
                .clickable(actionRunCallback<RefreshPortfolioAction>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u21bb",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_background),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun AssetRowCompact(asset: AssetWithPrice) {
    val profitLoss = asset.profitLoss ?: 0.0
    val isPositive = profitLoss >= 0
    val profitColorRes = if (isPositive) R.color.widget_profit else R.color.widget_loss
    val sign = if (isPositive) "+" else ""

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = asset.asset.symbol,
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_primary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = "$sign${profitLoss.formatCurrency()}",
            style = TextStyle(
                color = ColorProvider(profitColorRes),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun AssetRowDetailed(asset: AssetWithPrice) {
    val profitLoss = asset.profitLoss ?: 0.0
    val isPositive = profitLoss >= 0
    val profitColorRes = if (isPositive) R.color.widget_profit else R.color.widget_loss
    val sign = if (isPositive) "+" else ""
    val percentText = asset.profitLossPercent?.let { " (%${DecimalFormat("0.00").format(it)})" } ?: ""
    val quantityText = DecimalFormat("0.##").format(asset.totalQuantity)

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = asset.asset.symbol,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(1.dp))
            Text(
                text = "$quantityText adet",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (asset.currentValue ?: 0.0).formatCurrency(),
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(1.dp))
            Text(
                text = "$sign${profitLoss.formatCurrency()}$percentText",
                style = TextStyle(
                    color = ColorProvider(profitColorRes),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

private fun summaryPercent(profitLoss: Double?, totalValue: Double): String {
    val cost = (totalValue - (profitLoss ?: 0.0)).coerceAtLeast(0.0)
    return if (cost > 0) {
        val percent = (profitLoss ?: 0.0) / cost * 100
        " (%${DecimalFormat("0.00").format(percent)})"
    } else ""
}

class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}

class RefreshPortfolioAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as? SistApplication ?: return
        val assets = app.container.assetRepository.getAllAssets().first()
        if (assets.isNotEmpty()) {
            app.container.refreshAssetPricesUseCase(assets)
        }
    }
}
