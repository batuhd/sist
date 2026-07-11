package com.sinop.sist.domain.model

import java.time.YearMonth

data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null,
    val monthlyLimit: Double,
    val month: YearMonth
)

data class BudgetWithSpending(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentage: Float
)
