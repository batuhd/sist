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
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.first

class NetWorthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NetWorthWidget()
}

class NetWorthWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 50.dp),
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(300.dp, 200.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SistApplication
        val (total, accountBalance, portfolioValue) = if (app != null) {
            try {
                val accounts = WidgetDataProvider.getAccountsWithBalances(app)
                val portfolio = WidgetDataProvider.getPortfolioSummary(app)
                val accountTotal = accounts.sumOf { it.balance }
                val portfolioTotal = portfolio.totalValue ?: 0.0
                Triple(accountTotal + portfolioTotal, accountTotal, portfolioTotal)
            } catch (_: Exception) {
                Triple(0.0, 0.0, 0.0)
            }
        } else {
            Triple(0.0, 0.0, 0.0)
        }

        provideContent {
            NetWorthWidgetContent(
                totalNetWorth = total,
                accountBalance = accountBalance,
                portfolioValue = portfolioValue
            )
        }
    }
}

@Composable
private fun NetWorthWidgetContent(
    totalNetWorth: Double,
    accountBalance: Double,
    portfolioValue: Double
) {
    val size = LocalSize.current
    val compact = size.width <= 180.dp && size.height <= 50.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(R.color.widget_background))
            .clickable(actionRunCallback<OpenNetWorthAppAction>())
            .padding(16.dp)
    ) {
        if (compact) {
            NetWorthCompact(totalNetWorth = totalNetWorth)
        } else {
            NetWorthExpanded(
                totalNetWorth = totalNetWorth,
                accountBalance = accountBalance,
                portfolioValue = portfolioValue
            )
        }
    }
}

@Composable
private fun NetWorthCompact(totalNetWorth: Double) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "Toplam Varlık",
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_secondary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = totalNetWorth.formatCurrency(),
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_primary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun NetWorthExpanded(
    totalNetWorth: Double,
    accountBalance: Double,
    portfolioValue: Double
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Toplam Varlık",
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
                    .clickable(actionRunCallback<RefreshNetWorthAction>()),
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

        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = totalNetWorth.formatCurrency(),
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_primary),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(10.dp))
        Text(
            text = "Hesaplar: ${accountBalance.formatCurrency()}",
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_secondary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "Portföy: ${portfolioValue.formatCurrency()}",
            style = TextStyle(
                color = ColorProvider(R.color.widget_text_secondary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

class OpenNetWorthAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}

class RefreshNetWorthAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as? SistApplication ?: return
        val assets = app.container.assetRepository.getAllAssets().first()
        if (assets.isNotEmpty()) {
            app.container.refreshAssetPricesUseCase(assets)
        }
    }
}
