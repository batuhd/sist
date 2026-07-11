package com.sinop.sist.domain.model

import java.time.LocalDate

data class Installment(
    val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val installmentCount: Int,
    val monthlyAmount: Double,
    val startDate: LocalDate,
    val cardOrAccount: String? = null,
    val remainingCount: Int,
    val note: String? = null,
    val currencyCode: String = "TRY",
    val isCompleted: Boolean = false
)
