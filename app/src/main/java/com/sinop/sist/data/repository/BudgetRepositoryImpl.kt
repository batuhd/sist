package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.BudgetDao
import com.sinop.sist.data.local.dao.TransactionDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Budget
import com.sinop.sist.domain.model.BudgetWithSpending
import com.sinop.sist.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.YearMonth

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override suspend fun insert(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }

    override suspend fun update(budget: Budget) {
        budgetDao.update(budget.toEntity())
    }

    override suspend fun delete(budget: Budget) {
        budgetDao.delete(budget.toEntity())
    }

    override suspend fun getById(id: Long): Budget? {
        return budgetDao.getById(id)?.toDomain()
    }

    override fun getByMonth(month: YearMonth): Flow<List<BudgetWithSpending>> {
        val budgetsFlow = budgetDao.getByMonth(month.toString()).map { list -> list.map { it.toDomain() } }
        val start = month.atDay(1).atStartOfDay()
        val end = month.atEndOfMonth().atTime(23, 59, 59)

        return budgetsFlow.combine(transactionDao.getBetween(start, end)) { budgets, transactions ->
            budgets.map { budget ->
                val spent = if (budget.categoryId == null) {
                    transactions.filter { it.type == "expense" }.sumOf { it.amount }
                } else {
                    transactions.filter { it.type == "expense" && it.categoryId == budget.categoryId }.sumOf { it.amount }
                }
                val remaining = budget.monthlyLimit - spent
                val percentage = if (budget.monthlyLimit > 0) {
                    (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f)
                } else 0f
                BudgetWithSpending(budget, spent, remaining, percentage)
            }
        }
    }

    override suspend fun getByCategoryAndMonth(categoryId: Long?, month: YearMonth): Budget? {
        return budgetDao.getByCategoryAndMonth(categoryId, month.toString())?.toDomain()
    }
}
