package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.RecurringTransactionDao
import com.sinop.sist.data.local.dao.TransactionDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.RecurringTransaction
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

class RecurringTransactionRepositoryImpl(
    private val recurringDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) : RecurringTransactionRepository {

    override suspend fun insert(recurring: RecurringTransaction): Long {
        return recurringDao.insert(recurring.toEntity())
    }

    override suspend fun update(recurring: RecurringTransaction) {
        recurringDao.update(recurring.toEntity())
    }

    override suspend fun delete(recurring: RecurringTransaction) {
        recurringDao.delete(recurring.toEntity())
    }

    override suspend fun getById(id: Long): RecurringTransaction? {
        return recurringDao.getById(id)?.toDomain()
    }

    override fun getAllActive(): Flow<List<RecurringTransaction>> {
        return recurringDao.getAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<RecurringTransaction>> {
        return recurringDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun generateTransactionsForCurrentPeriod(): Int {
        val today = LocalDate.now()
        val list = recurringDao.getAllActive().first()
        var generated = 0
        list.forEach { recurring ->
            val domain = recurring.toDomain()
            if (shouldGenerateForToday(domain, today)) {
                val alreadyExists = transactionDao.getByRecurringIdAndDate(
                    recurringId = domain.id,
                    start = today.atStartOfDay(),
                    end = today.plusDays(1).atStartOfDay()
                ).isNotEmpty()
                if (!alreadyExists) {
                    val transaction = Transaction(
                        amount = domain.amount,
                        type = domain.type,
                        categoryId = domain.categoryId,
                        date = today.atStartOfDay(),
                        note = domain.title,
                        paymentMethod = domain.paymentMethod,
                        currencyCode = domain.currencyCode,
                        isRecurring = true,
                        recurringId = domain.id
                    )
                    transactionDao.insert(transaction.toEntity())
                    generated++
                }
            }
        }
        return generated
    }

    private fun shouldGenerateForToday(recurring: RecurringTransaction, today: LocalDate): Boolean {
        if (today.isBefore(recurring.startDate)) return false
        if (recurring.endDate != null && today.isAfter(recurring.endDate)) return false

        return when (recurring.period) {
            com.sinop.sist.domain.model.RecurrencePeriod.DAILY -> true
            com.sinop.sist.domain.model.RecurrencePeriod.WEEKLY -> today.dayOfWeek == recurring.startDate.dayOfWeek
            com.sinop.sist.domain.model.RecurrencePeriod.MONTHLY -> today.dayOfMonth == recurring.startDate.dayOfMonth
            com.sinop.sist.domain.model.RecurrencePeriod.YEARLY -> {
                today.month == recurring.startDate.month && today.dayOfMonth == recurring.startDate.dayOfMonth
            }
        }
    }
}
