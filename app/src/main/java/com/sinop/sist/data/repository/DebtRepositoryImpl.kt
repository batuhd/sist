package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.DebtDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Debt
import com.sinop.sist.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DebtRepositoryImpl(
    private val debtDao: DebtDao
) : DebtRepository {

    override suspend fun insert(debt: Debt): Long {
        return debtDao.insert(debt.toEntity())
    }

    override suspend fun update(debt: Debt) {
        debtDao.update(debt.toEntity())
    }

    override suspend fun delete(debt: Debt) {
        debtDao.delete(debt.toEntity())
    }

    override suspend fun getById(id: Long): Debt? {
        return debtDao.getById(id)?.toDomain()
    }

    override fun getAll(): Flow<List<Debt>> {
        return debtDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getUnpaid(): Flow<List<Debt>> {
        return debtDao.getUnpaid().map { list -> list.map { it.toDomain() } }
    }

    override fun getDueBetween(start: LocalDate, end: LocalDate): Flow<List<Debt>> {
        return debtDao.getDueBetween(start, end).map { list -> list.map { it.toDomain() } }
    }
}
