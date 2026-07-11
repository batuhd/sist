package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    suspend fun insert(recurring: RecurringTransaction): Long
    suspend fun update(recurring: RecurringTransaction)
    suspend fun delete(recurring: RecurringTransaction)
    suspend fun getById(id: Long): RecurringTransaction?
    fun getAllActive(): Flow<List<RecurringTransaction>>
    fun getAll(): Flow<List<RecurringTransaction>>
    suspend fun generateTransactionsForCurrentPeriod(): Int
}
