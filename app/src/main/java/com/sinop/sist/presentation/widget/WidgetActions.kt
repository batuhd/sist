package com.sinop.sist.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.actionStartActivity
import com.sinop.sist.SistApplication
import kotlinx.coroutines.flow.first

/**
 * Tum widget'larin ortak aksiyonlari.
 * Yenile aksiyonlari gercekten calisir: once fiyatlar yenilenir,
 * sonra ilgili widget'lar yeniden cizilir.
 */

class OpenSistAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}

/**
 * Widget'in tema ve yazi boyutu ayarlarini acan aksiyon.
 */
fun widgetSettingsAction(context: Context, widgetId: Int): Action = actionStartActivity(
    Intent(context, WidgetThemeConfigureActivity::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
)

suspend fun GlanceAppWidget.updateAllInstances(context: Context) {
    try {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(this::class.java).forEach { glanceId ->
            update(context, glanceId)
        }
    } catch (_: Exception) {
        // Widget yoksa sessizce gec
    }
}

class RefreshPortfolioAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as? SistApplication ?: return
        val assets = app.container.assetRepository.getAllAssets().first()
        if (assets.isNotEmpty()) {
            app.container.refreshAssetPricesUseCase(assets)
        }
        PortfolioWidget().updateAllInstances(context)
        NetWorthWidget().updateAllInstances(context)
        DistributionWidget().updateAllInstances(context)
        WatchlistWidget().updateAllInstances(context)
    }
}

class RefreshBudgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        BudgetWidget().updateAllInstances(context)
    }
}

class RefreshDistributionAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        DistributionWidget().updateAllInstances(context)
    }
}

class RefreshWatchlistAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        WatchlistWidget().updateAllInstances(context)
    }
}
