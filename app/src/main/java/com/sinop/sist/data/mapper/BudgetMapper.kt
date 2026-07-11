package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.BudgetEntity
import com.sinop.sist.domain.model.Budget
import java.time.YearMonth

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
    month = YearMonth.parse(month)
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
    month = month.toString()
)
