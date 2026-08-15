package com.sinop.sist.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.action.Action
import androidx.glance.appwidget.AppWidgetId
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.ColorFilter
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
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
import com.sinop.sist.R
import com.sinop.sist.SistApplication
import com.sinop.sist.util.formatCurrency
import java.time.LocalDate

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}

class BudgetWidget : GlanceAppWidget() {

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
        val snapshot = try {
            val app = context.applicationContext as? SistApplication
            if (app != null) WidgetDataProvider.getBudgetSnapshot(app) else null
        } catch (_: Exception) {
            null
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                    val widgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
                    val settingsAction = widgetSettingsAction(context, widgetId)
                SistWidgetContainer(openAppAction = actionRunCallback<OpenSistAppAction>()) {
                    if (snapshot != null && snapshot.hasBudgets) {
                        BudgetWidgetContent(snapshot, settingsAction)
                    } else {
                        BudgetEmptyState()
                    }
                }
            }
        }
    }
}

/**
 * Kompakt bos durum: buyuk bosluk birakmaz, yonlendirme buton gorunumunde.
 */
@Composable
private fun BudgetEmptyState() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Henüz bütçe oluşturmadın",
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (13 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.height(3.dp))
        Text(
            text = "Aylık limitini belirle, harcamanı takip et",
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1
        )
        Spacer(GlanceModifier.height(8.dp))
        WidgetPillButton(
            label = "Bütçe Oluştur",
            action = actionRunCallback<OpenSistAppAction>()
        )
    }
}

/**
 * Ayın kaçıncı gününde olduğumuza göre basit doğrusal projeksiyon:
 * bu hızla giderse ay sonunda bütçe aşılır mı?
 */
private fun projectedOvershoot(snapshot: WidgetDataProvider.BudgetSnapshot): Boolean {
    val general = snapshot.general ?: return false
    val spent = general.spent
    val limit = general.budget.monthlyLimit
    if (limit <= 0.0 || spent <= 0.0) return false
    val today = LocalDate.now()
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = today.lengthOfMonth()
    val projected = spent / dayOfMonth * daysInMonth
    return projected > limit && general.remaining > 0
}

@Composable
private fun BudgetWidgetContent(snapshot: WidgetDataProvider.BudgetSnapshot, settingsAction: Action) {
    val size = LocalSize.current
    val general = snapshot.general

    when {
        size.height <= 50.dp && general != null -> BudgetCompact(general)
        size.height <= 110.dp && general != null -> BudgetMedium(general, snapshot, settingsAction)
        size.height <= 170.dp && general != null -> BudgetExpanded(general, snapshot, settingsAction)
        else -> BudgetFull(snapshot, settingsAction)
    }
}

@Composable
private fun progressColorFor(percentage: Float) = when {
    percentage >= 1f -> SistWidgetColors.onHeroNegative
    percentage >= 0.85f -> SistWidgetColors.onHeroWarning
    else -> SistWidgetColors.onHeroPositive
}

@Composable
private fun BudgetCompact(general: com.sinop.sist.domain.model.BudgetWithSpending) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "Aylık Bütçe",
            style = TextStyle(
                color = SistWidgetColors.onHeroMuted,
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        WidgetAmount(text = general.spent.formatCurrency(), fontSize = 18, color = SistWidgetColors.onHero)
        Spacer(GlanceModifier.height(3.dp))
        WidgetProgress(
            progress = general.percentage,
            color = progressColorFor(general.percentage)
        )
    }
}

@Composable
private fun BudgetMedium(
    general: com.sinop.sist.domain.model.BudgetWithSpending,
    snapshot: WidgetDataProvider.BudgetSnapshot,
    settingsAction: Action
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Aylık Bütçe", refreshAction = actionRunCallback<RefreshBudgetAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            WidgetAmount(text = general.spent.formatCurrency(), fontSize = 22, color = SistWidgetColors.onHero)
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "/ ${general.budget.monthlyLimit.formatCurrency()}",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (13 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        WidgetProgress(
            progress = general.percentage,
            color = progressColorFor(general.percentage)
        )
        Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Kalan: ${general.remaining.formatCurrency()}",
                style = TextStyle(
                    color = progressColorFor(general.percentage),
                    fontSize = (12 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            if (projectedOvershoot(snapshot)) {
                OvershootChip()
            }
        }
    }
}

@Composable
private fun OvershootChip() {
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
            text = "Bu hızla aşılabilir",
            style = TextStyle(
                color = SistWidgetColors.onHeroWarning,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun BudgetExpanded(
    general: com.sinop.sist.domain.model.BudgetWithSpending,
    snapshot: WidgetDataProvider.BudgetSnapshot,
    settingsAction: Action
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Aylık Bütçe", refreshAction = actionRunCallback<RefreshBudgetAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            WidgetAmount(text = general.spent.formatCurrency(), fontSize = 24, color = SistWidgetColors.onHero)
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "/ ${general.budget.monthlyLimit.formatCurrency()}",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (13 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
            if (projectedOvershoot(snapshot)) {
                Spacer(GlanceModifier.width(6.dp))
                OvershootChip()
            }
        }
        Spacer(GlanceModifier.height(6.dp))
        WidgetProgress(
            progress = general.percentage,
            color = progressColorFor(general.percentage)
        )
        Spacer(GlanceModifier.height(3.dp))
        Text(
            text = "Kalan: ${general.remaining.formatCurrency()} - %${"%.0f".format(general.percentage.coerceIn(0f, 1f) * 100)} kullanıldı",
            style = TextStyle(
                color = progressColorFor(general.percentage),
                fontSize = (12 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            )
        )

        if (snapshot.categoryBudgets.isNotEmpty()) {
            Spacer(GlanceModifier.height(8.dp))
            Column {
                snapshot.categoryBudgets.take(2).forEach { (name, budget) ->
                    CategoryBudgetRow(name, budget)
                    Spacer(GlanceModifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetFull(snapshot: WidgetDataProvider.BudgetSnapshot, settingsAction: Action) {
    val general = snapshot.general
    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Aylık Bütçe", refreshAction = actionRunCallback<RefreshBudgetAction>(), settingsAction = settingsAction)

        if (general != null) {
            Spacer(GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                WidgetAmount(text = general.spent.formatCurrency(), fontSize = 26, color = SistWidgetColors.onHero)
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "/ ${general.budget.monthlyLimit.formatCurrency()}",
                    style = TextStyle(
                        color = SistWidgetColors.onHeroMuted,
                        fontSize = (13 * SistWidgetFonts.scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (projectedOvershoot(snapshot)) {
                    Spacer(GlanceModifier.width(6.dp))
                    OvershootChip()
                }
            }
            Spacer(GlanceModifier.height(6.dp))
            WidgetProgress(
                progress = general.percentage,
                color = progressColorFor(general.percentage)
            )
            Spacer(GlanceModifier.height(3.dp))
            Text(
                text = "Kalan: ${general.remaining.formatCurrency()} - %${"%.0f".format(general.percentage.coerceIn(0f, 1f) * 100)} kullanıldı",
                style = TextStyle(
                    color = progressColorFor(general.percentage),
                    fontSize = (12 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        if (snapshot.categoryBudgets.isNotEmpty()) {
            Column {
                Spacer(GlanceModifier.height(10.dp))
                Text(
                    text = "Kategoriler",
                    style = TextStyle(
                        color = SistWidgetColors.onHeroMuted,
                        fontSize = (11 * SistWidgetFonts.scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Column {
                    snapshot.categoryBudgets.take(4).forEach { (name, budget) ->
                        CategoryBudgetRow(name, budget)
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        } else {
            Spacer(GlanceModifier.height(10.dp))
            WidgetEmptyState(
                title = "Kategori bütçesi yok",
                subtitle = "Kategori bütçesi eklemek için dokun"
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(name: String, budget: com.sinop.sist.domain.model.BudgetWithSpending) {
    val color = progressColorFor(budget.percentage)
    val over = budget.percentage >= 1f

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = name,
                style = TextStyle(
                    color = SistWidgetColors.onHero,
                    fontSize = (12 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "${budget.spent.formatCurrency()} / ${budget.budget.monthlyLimit.formatCurrency()}",
                style = TextStyle(
                    color = color,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
            if (over) {
                Spacer(GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_alert),
                    contentDescription = "Limit aşıldı",
                    modifier = GlanceModifier.size(10.dp),
                    colorFilter = ColorFilter.tint(SistWidgetColors.onHeroNegative)
                )
            }
        }
        Spacer(GlanceModifier.height(3.dp))
        WidgetProgress(progress = budget.percentage, color = color)
    }
}
