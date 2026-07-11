package com.sinop.sist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sinop.sist.data.local.entity.InstallmentEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InstallmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(installment: InstallmentEntity): Long

    @Update
    suspend fun update(installment: InstallmentEntity)

    @Delete
    suspend fun delete(installment: InstallmentEntity)

    @Query("SELECT * FROM installments WHERE isCompleted = 0 ORDER BY startDate ASC")
    fun getActive(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments ORDER BY startDate DESC")
    fun getAll(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE id = :id")
    suspend fun getById(id: Long): InstallmentEntity?

    @Query("SELECT * FROM installments WHERE startDate <= :end AND isCompleted = 0 ORDER BY startDate ASC")
    fun getActiveUntil(end: LocalDate): Flow<List<InstallmentEntity>>
}
