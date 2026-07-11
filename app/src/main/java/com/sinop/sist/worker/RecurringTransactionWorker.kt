package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.repository.RecurringTransactionRepository
import com.sinop.sist.util.NotificationHelper
import java.time.LocalDate

class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SistApplication ?: return Result.success()
        val repository = app.container.recurringTransactionRepository

        val generated = repository.generateTransactionsForCurrentPeriod()
        if (generated > 0) {
            NotificationHelper.showNotification(
                context = applicationContext,
                title = "Tekrarlayan İşlemler",
                message = "$generated adet tekrarlayan işlem bugün için oluşturuldu.",
                notificationId = 2000
            )
        }
        return Result.success()
    }
}
