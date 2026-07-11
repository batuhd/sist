package com.sinop.sist.domain.model

import java.time.LocalDate

data class RecurringTransaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val title: String,
    val period: RecurrencePeriod,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val paymentMethod: PaymentMethod? = null,
    val currencyCode: String = "TRY",
    val isActive: Boolean = true
)

enum class RecurrencePeriod { DAILY, WEEKLY, MONTHLY, YEARLY }
