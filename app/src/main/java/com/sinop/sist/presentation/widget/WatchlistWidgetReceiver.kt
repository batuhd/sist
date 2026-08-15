package com.sinop.sist.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sinop.sist.SistApplication
import com.sinop.sist.util.formatCurrency
import java.util.Locale

class WatchlistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WatchlistWidget()
}

class WatchlistWidget : GlanceAppWidget() {

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
        val widgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
        // Disli ikonu sembol yapilandirma ekranini acar (ilk kurulumdaki ekranla ayni).
        val settingsAction = actionStartActivity(
            Intent(context, WatchlistConfigureActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        )
        val rows = if (app != null && widgetId >= 0) {
            try {
                WidgetDataProvider.getWatchlistRows(context, app, widgetId)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        provideContent {
            SistWidgetTheme(palette = WidgetThemePrefs.paletteFor(WidgetThemePrefs.themeFor(context, id)), fontScale = WidgetThemePrefs.fontScaleFor(context, id)) {
                SistWidgetContainer(openAppAction = actionRunCallback<OpenSistAppAction>()) {
                    WatchlistWidgetContent(rows = rows, settingsAction = settingsAction)
                }
            }
        }
    }
}

@Composable
private fun WatchlistWidgetContent(
    rows: List<WidgetDataProvider.WatchlistRow>,
    settingsAction: androidx.glance.action.Action
) {
    val size = LocalSize.current

    if (rows.isEmpty()) {
        WidgetEmptyState(
            title = "Takip edilecek sembol yok",
            subtitle = "Basılı tutup ayarlardan sembol ekleyin"
        )
        return
    }

    val visibleCount = when {
        size.height <= 110.dp -> 3
        size.height <= 170.dp -> 4
        else -> rows.size
    }

    Column(modifier = GlanceModifier.fillMaxSize()) {
        SistWidgetHeader(title = "Takip Listesi", refreshAction = actionRunCallback<RefreshWatchlistAction>(), settingsAction = settingsAction)
        Spacer(GlanceModifier.height(6.dp))
        Column {
            rows.take(visibleCount).forEach { row ->
                WatchlistRow(row)
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun WatchlistRow(row: WidgetDataProvider.WatchlistRow) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionRunCallback<OpenSistAppAction>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = row.symbol,
                style = TextStyle(
                    color = SistWidgetColors.onHero,
                    fontSize = (14 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Text(
                text = sourceLabel(row),
                style = TextStyle(
                    color = SistWidgetColors.onHeroMuted,
                    fontSize = (11 * SistWidgetFonts.scale).sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
        }
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = row.price?.formatCurrency() ?: "-",
            style = TextStyle(
                color = SistWidgetColors.onHero,
                fontSize = (14 * SistWidgetFonts.scale).sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

private fun sourceLabel(row: WidgetDataProvider.WatchlistRow): String {
    val source = row.source
    val updated = row.updated
    val sourceText = when {
        source?.startsWith("fvt") == true -> "FVT"
        source?.startsWith("yahoo") == true -> "Yahoo"
        source.isNullOrBlank() -> ""
        else -> source
    }
    return if (updated != null) {
        String.format(Locale.forLanguageTag("tr-TR"), "%s - %02d:%02d", sourceText, updated.hour, updated.minute)
    } else {
        sourceText
    }
}
