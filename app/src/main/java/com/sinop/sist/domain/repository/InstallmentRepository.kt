package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Installment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface InstallmentRepository {
    suspend fun insert(installment: Installment): Long
    suspend fun update(installment: Installment)
    suspend fun delete(installment: Installment)
    suspend fun getById(id: Long): Installment?
    fun getActive(): Flow<List<Installment>>
    fun getAll(): Flow<List<Installment>>
    fun getActiveUntil(end: LocalDate): Flow<List<Installment>>
}
