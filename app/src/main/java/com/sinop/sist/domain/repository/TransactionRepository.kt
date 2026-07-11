package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TransactionRepository {
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun getById(id: Long): Transaction?
    fun getAll(): Flow<List<Transaction>>
    fun getBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<Transaction>>
    fun getByTypeAndPeriod(type: TransactionType, start: LocalDateTime, end: LocalDateTime): Flow<List<Transaction>>
    fun getSumByTypeAndPeriod(type: TransactionType, start: LocalDateTime, end: LocalDateTime): Flow<Double>
    fun getByAccount(accountId: Long): Flow<List<Transaction>>
}
