package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Debt
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DebtRepository {
    suspend fun insert(debt: Debt): Long
    suspend fun update(debt: Debt)
    suspend fun delete(debt: Debt)
    suspend fun getById(id: Long): Debt?
    fun getAll(): Flow<List<Debt>>
    fun getUnpaid(): Flow<List<Debt>>
    fun getDueBetween(start: LocalDate, end: LocalDate): Flow<List<Debt>>
}
