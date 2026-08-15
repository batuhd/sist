package com.sinop.sist.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
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
import com.sinop.sist.domain.model.Category

class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

object QuickAddActivityContract {
    const val EXTRA_TYPE = "sist_extra_type"
    const val EXTRA_CATEGORY_ID = "sist_extra_category_id"
    const val TYPE_INCOME = "INCOME"
    const val TYPE_EXPENSE = "EXPENSE"
    const val TYPE_TRANSFER = "TRANSFER"
}

class QuickAddWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 50.dp),
            DpSize(180.dp, 110.dp),
            DpSize(300.dp, 50.dp),
            DpSize(300.dp, 110.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? SistApplication
        val widgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
        val settingsAction = widgetSettingsAction(context, widgetId)
        val topCategories = if (app != null) {
            try {
                WidgetDataProvider.getTopExpenseCategories(app, 3)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(28.dp)
                        .background(SistWidgetColors.hero)
                ) {
                    QuickAddWidgetContent(topCategories)
                    // Ayarlar: transfer butonunun sağ üstünde duran küçük dişli.
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(2.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(24.dp)
                                .cornerRadius(12.dp)
                                .background(SistWidgetColors.heroVariant)
                                .clickable(settingsAction),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_settings),
                                contentDescription = "Ayarlar",
                                modifier = GlanceModifier.size(13.dp),
                                colorFilter = ColorFilter.tint(SistWidgetColors.onHeroMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddWidgetContent(topCategories: List<Category>) {
    val context = LocalContext.current
    val size = LocalSize.current
    val wide = size.width > 200.dp
    val tall = size.height >= 110.dp
    val showChips = tall && topCategories.isNotEmpty()

    Column(modifier = GlanceModifier.fillMaxSize().padding(4.dp)) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuickAddActionButton(
                label = "Gelir",
                iconRes = R.drawable.ic_widget_plus,
                color = SistWidgetColors.onHeroPositive,
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                action = actionStartActivity(quickAddIntent(context, QuickAddActivityContract.TYPE_INCOME, null))
            )
            Spacer(GlanceModifier.width(8.dp))
            QuickAddActionButton(
                label = "Gider",
                iconRes = R.drawable.ic_widget_minus,
                color = SistWidgetColors.onHeroNegative,
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                action = actionStartActivity(quickAddIntent(context, QuickAddActivityContract.TYPE_EXPENSE, null))
            )
            if (wide) {
                Spacer(GlanceModifier.width(8.dp))
                QuickAddActionButton(
                    label = "Transfer",
                    iconRes = R.drawable.ic_widget_swap,
                    color = SistWidgetColors.onHeroGold,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    action = actionStartActivity(quickAddIntent(context, QuickAddActivityContract.TYPE_TRANSFER, null))
                )
            }
        }

        if (showChips) {
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "Sık kullanılanlar",
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                topCategories.forEachIndexed { index, category ->
                    if (index > 0) Spacer(GlanceModifier.width(4.dp))
                    CategoryChip(context, category)
                }
            }
        }
    }
}

private fun quickAddIntent(context: Context, type: String, categoryId: Long?): Intent {
    return Intent(context, QuickAddActivity::class.java)
        .putExtra(QuickAddActivityContract.EXTRA_TYPE, type)
        .apply {
            if (categoryId != null) {
                putExtra(QuickAddActivityContract.EXTRA_CATEGORY_ID, categoryId)
            }
        }
}

@Composable
private fun QuickAddActionButton(
    label: String,
    iconRes: Int,
    color: ColorProvider,
    modifier: GlanceModifier,
    action: Action
) {
    Box(
        modifier = modifier
            .cornerRadius(16.dp)
            .background(SistWidgetColors.heroVariant)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                colorFilter = ColorFilter.tint(color)
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = label,
                style = TextStyle(
                    color = color,
                    fontSize = (13 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryChip(context: Context, category: Category) {
    Box(
        modifier = GlanceModifier
            .cornerRadius(12.dp)
            .background(SistWidgetColors.heroVariant)
            .clickable(
                actionStartActivity(
                    quickAddIntent(context, QuickAddActivityContract.TYPE_EXPENSE, category.id)
                )
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name,
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (11 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}
