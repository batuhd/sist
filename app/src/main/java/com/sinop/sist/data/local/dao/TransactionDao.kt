package com.sinop.sist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sinop.sist.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByTypeAndPeriod(type: String, start: LocalDateTime, end: LocalDateTime): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByCategoryAndPeriod(categoryId: Long, start: LocalDateTime, end: LocalDateTime): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    fun getSumByTypeAndPeriod(type: String, start: LocalDateTime, end: LocalDateTime): Flow<Double?>

    @Query("DELETE FROM transactions WHERE recurringId = :recurringId")
    suspend fun deleteByRecurringId(recurringId: Long)

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE recurringId = :recurringId AND date BETWEEN :start AND :end")
    suspend fun getByRecurringIdAndDate(
        recurringId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<TransactionEntity>
}
