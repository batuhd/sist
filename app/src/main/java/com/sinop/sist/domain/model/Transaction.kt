package com.sinop.sist.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long? = null,
    val date: LocalDateTime,
    val note: String? = null,
    val tags: List<String> = emptyList(),
    val paymentMethod: PaymentMethod? = null,
    val currencyCode: String = "TRY",
    val isRecurring: Boolean = false,
    val recurringId: Long? = null
)

enum class TransactionType { INCOME, EXPENSE }

enum class PaymentMethod { CASH, BANK }
