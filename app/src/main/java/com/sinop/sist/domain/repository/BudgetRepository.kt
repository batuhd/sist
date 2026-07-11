package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Budget
import com.sinop.sist.domain.model.BudgetWithSpending
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    suspend fun insert(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
    suspend fun getById(id: Long): Budget?
    fun getByMonth(month: YearMonth): Flow<List<BudgetWithSpending>>
    suspend fun getByCategoryAndMonth(categoryId: Long?, month: YearMonth): Budget?
}
