package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.util.CrashLogger
import com.sinop.sist.util.NotificationHelper
import com.sinop.sist.util.formatCurrency
import com.sinop.sist.util.formatPercent
import com.sinop.sist.util.formatSignedCurrency
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MarketCloseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        CrashLogger.d(TAG, "Market kapanış bildirimi çalışıyor")
        val app = applicationContext as? SistApplication ?: return Result.failure()
        val repository = app.container.assetRepository
        val refreshUseCase = app.container.refreshAssetPricesUseCase

        try {
            val allAssets = repository.getAllAssets().first()
            if (allAssets.isEmpty()) {
                CrashLogger.d(TAG, "Varlık yok, bildirim atlanıyor")
                return Result.success()
            }

            try {
                refreshUseCase(allAssets)
            } catch (e: Exception) {
                CrashLogger.w(TAG, "Fiyat yenileme başarısız, önbellek kullanılacak", e)
            }

            val assets = repository.getAssetsWithPrices().first()
            val assetsWithHoldings = assets.filter { it.totalQuantity > 0 && it.currentPrice != null }

            if (assetsWithHoldings.isEmpty()) {
                return Result.success()
            }

            assetsWithHoldings.forEachIndexed { index, asset ->
                val profitLoss = asset.profitLoss ?: 0.0
                val isPositive = profitLoss >= 0
                val percentText = asset.profitLossPercent?.let { " (${it.formatPercent()})" } ?: ""

                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "${asset.asset.symbol} kapanış: ${asset.currentPrice!!.formatCurrency()}",
                    message = "${if (isPositive) "Kâr" else "Zarar"}: ${abs(profitLoss).let {
                        if (isPositive) it.formatSignedCurrency() else "-${it.formatCurrency()}"
                    }}$percentText",
                    notificationId = MARKET_CLOSE_NOTIFICATION_BASE_ID + index
                )
            }

            return Result.success()
        } catch (e: Exception) {
            CrashLogger.e(TAG, "Market kapanış bildirimi hatası", e)
            return Result.failure()
        } finally {
            scheduleNext(applicationContext)
        }
    }

    companion object {
        const val WORK_NAME = "market_close_notification"
        private const val TAG = "MarketCloseNotificationWorker"
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
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            CrashLogger.d(TAG, "Sonraki çalışma planlandı: $nextTrigger")
        }

        suspend fun ensureScheduled(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val infos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
            val hasActive = infos.any {
                it.state == WorkInfo.State.ENQUEUED ||
                        it.state == WorkInfo.State.RUNNING ||
                        it.state == WorkInfo.State.BLOCKED
            }
            if (!hasActive) {
                CrashLogger.w(TAG, "Zincir kopmuş, yeniden planlanıyor")
                scheduleNext(context)
            }
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
