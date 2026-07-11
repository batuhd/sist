package com.sinop.sist.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.repository.BudgetRepository
import com.sinop.sist.util.NotificationHelper
import com.sinop.sist.util.formatCurrency
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class BudgetCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? SistApplication ?: return Result.success()
        val repository = app.container.budgetRepository

        checkBudgets(repository, applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "budget_check_worker"

        suspend fun checkBudgets(repository: BudgetRepository, context: Context) {
            val month = YearMonth.now()
            val budgets = repository.getByMonth(month).first()
            var notificationId = 1000

            budgets.forEach { budgetWithSpending ->
                val percentage = budgetWithSpending.percentage
                val title = when {
                    percentage >= 1f -> "Bütçe Aşıldı"
                    percentage >= 0.85f -> "Bütçe Uyarısı"
                    else -> null
                }
                if (title != null) {
                    val categoryName = if (budgetWithSpending.budget.categoryId == null) {
                        "Genel bütçe"
                    } else {
                        "Kategori bütçesi"
                    }
                    val message = buildString {
                        append(categoryName)
                        append(" için aylık limitin %")
                        append((percentage * 100).toInt())
                        append("'ine ulaştınız. ")
                        append("Harcanan: ${budgetWithSpending.spent.formatCurrency()}, ")
                        append("Limit: ${budgetWithSpending.budget.monthlyLimit.formatCurrency()}")
                    }
                    NotificationHelper.showNotification(
                        context = context,
                        title = title,
                        message = message,
                        notificationId = notificationId++
                    )
                }
            }
        }
    }
}
