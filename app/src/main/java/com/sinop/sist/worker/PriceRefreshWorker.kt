package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.util.CrashLogger
import com.sinop.sist.domain.usecase.RefreshAssetPricesUseCase
import com.sinop.sist.domain.repository.AssetRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class PriceRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SistApplication ?: return Result.failure()
        try {
            refresh(app.container.assetRepository, app.container.refreshAssetPricesUseCase)
            return Result.success()
        } catch (e: Exception) {
            CrashLogger.e(TAG, "Fiyat yenileme hatası", e)
            return Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "price_refresh_worker"
        private const val TAG = "PriceRefreshWorker"

        suspend fun refresh(
            assetRepository: AssetRepository,
            refreshAssetPricesUseCase: RefreshAssetPricesUseCase
        ) {
            val assets = assetRepository.getAllAssets().first()
            if (assets.isEmpty()) return
            refreshAssetPricesUseCase(assets)
        }

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PriceRefreshWorker>(2, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
