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
import java.time.YearMonth

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}

class BudgetWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 50.dp),
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 110.dp),
            DpSize(300.dp, 200.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = try {
            val app = context.applicationContext as? SistApplication
            val budgets = app?.container?.budgetRepository?.getByMonth(YearMonth.now())?.first()
            budgets?.find { it.budget.categoryId == null }
        } catch (_: Exception) {
            null
        }

        provideContent {
            if (data != null) {
                BudgetWidgetContent(
                    limit = data.budget.monthlyLimit,
                    spent = data.spent,
                    remaining = data.remaining,
                    percentage = data.percentage
                )
            } else {
                BudgetWidgetEmpty()
            }
        }
    }
}

@Composable
private fun BudgetWidgetContent(
    limit: Double,
    spent: Double,
    remaining: Double,
    percentage: Float
) {
    val size = LocalSize.current
    val progressColorRes = when {
        percentage >= 1f -> R.color.widget_loss
        percentage >= 0.85f -> R.color.widget_warning
        else -> R.color.widget_profit
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(R.color.widget_background))
            .clickable(actionRunCallback<OpenBudgetAppAction>())
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Aylık Bütçe",
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
                        .clickable(actionRunCallback<RefreshBudgetAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↻",
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
                text = spent.formatCurrency(),
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_primary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "/ ${limit.formatCurrency()}",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            if (size.height > 50.dp) {
                Spacer(GlanceModifier.height(10.dp))
                Text(
                    text = "%${"%.1f".format(percentage.coerceIn(0f, 1f) * 100)} kullanıldı",
                    style = TextStyle(
                        color = ColorProvider(progressColorRes),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "Kalan: ${remaining.formatCurrency()}",
                    style = TextStyle(
                        color = ColorProvider(progressColorRes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun BudgetWidgetEmpty() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(24.dp)
            .background(ColorProvider(R.color.widget_background))
            .clickable(actionRunCallback<OpenBudgetAppAction>())
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Aylık Bütçe",
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
                        .clickable(actionRunCallback<RefreshBudgetAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↻",
                        style = TextStyle(
                            color = ColorProvider(R.color.widget_background),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(GlanceModifier.height(10.dp))
            Text(
                text = "Bütçe tanımlamak için dokun",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_text_secondary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

class OpenBudgetAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}

class RefreshBudgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // Budget data is computed from transactions; widget refresh re-reads latest state.
    }
}
