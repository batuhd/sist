package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.PriceAlert
import com.sinop.sist.util.CrashLogger
import com.sinop.sist.util.NotificationHelper
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val app = applicationContext as? SistApplication ?: return Result.failure()
            val container = app.container

            val priceAlertsEnabled = container.notificationSettingsRepository.isPriceAlertsEnabled().first()
            if (!priceAlertsEnabled) {
                return Result.success()
            }

            val alerts = container.priceAlertRepository.getActiveAlerts().first()
            if (alerts.isEmpty()) {
                return Result.success()
            }

            val assets = container.assetRepository.getAssetsWithPrices().first()
            val assetMap = assets.associateBy { it.asset.id }

            alerts.forEach { alert ->
                val assetWithPrice = assetMap[alert.assetId] ?: return@forEach
                val currentPrice = assetWithPrice.currentPrice ?: return@forEach
                val symbol = assetWithPrice.asset.symbol

                val triggered = if (alert.isAbove) {
                    currentPrice >= alert.targetPrice
                } else {
                    currentPrice <= alert.targetPrice
                }

                if (triggered) {
                    val direction = if (alert.isAbove) "üzerine çıktı" else "altına düştü"
                    NotificationHelper.showNotification(
                        context = applicationContext,
                        title = "💰 Fiyat Alarmı: $symbol",
                        message = "$symbol hedef fiyat ${alert.targetPrice.formatCurrency()} $direction. Güncel fiyat: ${currentPrice.formatCurrency()}",
                        notificationId = alert.id.toInt()
                    )
                    container.priceAlertRepository.markAlertTriggered(alert.id)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            CrashLogger.e(TAG, "PriceAlertWorker failed", e)
            return Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "price_alert_worker"
        private const val TAG = "PriceAlertWorker"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
