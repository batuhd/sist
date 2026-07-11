package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.InstallmentDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Installment
import com.sinop.sist.domain.repository.InstallmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class InstallmentRepositoryImpl(
    private val installmentDao: InstallmentDao
) : InstallmentRepository {

    override suspend fun insert(installment: Installment): Long {
        return installmentDao.insert(installment.toEntity())
    }

    override suspend fun update(installment: Installment) {
        installmentDao.update(installment.toEntity())
    }

    override suspend fun delete(installment: Installment) {
        installmentDao.delete(installment.toEntity())
    }

    override suspend fun getById(id: Long): Installment? {
        return installmentDao.getById(id)?.toDomain()
    }

    override fun getActive(): Flow<List<Installment>> {
        return installmentDao.getActive().map { list -> list.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<Installment>> {
        return installmentDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveUntil(end: LocalDate): Flow<List<Installment>> {
        return installmentDao.getActiveUntil(end).map { list -> list.map { it.toDomain() } }
    }
}
