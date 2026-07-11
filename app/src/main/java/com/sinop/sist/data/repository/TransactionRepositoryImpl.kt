package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.TransactionDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction.toEntity())
    }

    override suspend fun getById(id: Long): Transaction? {
        return transactionDao.getById(id)?.toDomain()
    }

    override fun getAll(): Flow<List<Transaction>> {
        return transactionDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<Transaction>> {
        return transactionDao.getBetween(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun getByTypeAndPeriod(
        type: TransactionType,
        start: LocalDateTime,
        end: LocalDateTime
    ): Flow<List<Transaction>> {
        return transactionDao.getByTypeAndPeriod(type.name.lowercase(), start, end)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getSumByTypeAndPeriod(
        type: TransactionType,
        start: LocalDateTime,
        end: LocalDateTime
    ): Flow<Double> {
        return transactionDao.getSumByTypeAndPeriod(type.name.lowercase(), start, end)
            .map { it ?: 0.0 }
    }

    override fun getByAccount(accountId: Long): Flow<List<Transaction>> {
        return transactionDao.getByAccount(accountId).map { list -> list.map { it.toDomain() } }
    }
}
