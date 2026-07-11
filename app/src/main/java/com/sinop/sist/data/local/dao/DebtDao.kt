package com.sinop.sist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sinop.sist.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getUnpaid(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE dueDate BETWEEN :start AND :end AND isPaid = 0 ORDER BY dueDate ASC")
    fun getDueBetween(start: LocalDate, end: LocalDate): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): DebtEntity?
}
