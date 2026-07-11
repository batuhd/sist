package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.util.NotificationHelper
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MarketCloseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SistApplication ?: return Result.failure()
        val repository = app.container.assetRepository
        val refreshUseCase = app.container.refreshAssetPricesUseCase

        try {
            val allAssets = repository.getAllAssets().first()
            if (allAssets.isEmpty()) {
                scheduleNext(applicationContext)
                return Result.success()
            }

            refreshUseCase(allAssets)

            val assets = repository.getAssetsWithPrices().first()
            val assetsWithHoldings = assets.filter { it.totalQuantity > 0 && it.currentPrice != null }

            if (assetsWithHoldings.isEmpty()) {
                scheduleNext(applicationContext)
                return Result.success()
            }

            assetsWithHoldings.forEachIndexed { index, asset ->
                val profitLoss = asset.profitLoss ?: 0.0
                val isPositive = profitLoss >= 0
                val sign = if (isPositive) "+" else ""
                val percentText = asset.profitLossPercent?.let { " (%${"%.2f".format(it)})" } ?: ""

                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "${asset.asset.symbol} kapanış: ${asset.currentPrice!!.formatCurrency()}",
                    message = "Kar/Zarar: $sign${profitLoss.formatCurrency()}$percentText",
                    notificationId = MARKET_CLOSE_NOTIFICATION_BASE_ID + index
                )
            }

            scheduleNext(applicationContext)
            return Result.success()
        } catch (e: Exception) {
            // Retry once; if it keeps failing schedule next run anyway.
            scheduleNext(applicationContext)
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "market_close_notification"
        private const val MARKET_CLOSE_NOTIFICATION_BASE_ID = 200_000
        private val NOTIFICATION_TIME: LocalTime = LocalTime.of(18, 30)
        private val ZONE: ZoneId = ZoneId.of("Europe/Istanbul")

        fun scheduleNext(context: Context) {
            val now = LocalDateTime.now(ZONE)
            var nextTrigger = now.with(NOTIFICATION_TIME)
            if (!nextTrigger.isAfter(now)) {
                nextTrigger = nextTrigger.plusDays(1)
            }
            val delayMillis = Duration.between(now, nextTrigger).toMillis().coerceAtLeast(60_000)

            val request = OneTimeWorkRequestBuilder<MarketCloseNotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
