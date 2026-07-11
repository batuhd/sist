package com.sinop.sist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sinop.sist.data.local.entity.AssetTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: AssetTransactionEntity): Long

    @Update
    suspend fun update(transaction: AssetTransactionEntity)

    @Delete
    suspend fun delete(transaction: AssetTransactionEntity)

    @Query("SELECT * FROM asset_transactions WHERE assetId = :assetId ORDER BY transactionDate DESC")
    fun getByAssetId(assetId: Long): Flow<List<AssetTransactionEntity>>

    @Query("SELECT * FROM asset_transactions ORDER BY transactionDate DESC")
    fun getAll(): Flow<List<AssetTransactionEntity>>

    @Query("SELECT * FROM asset_transactions WHERE id = :id")
    suspend fun getById(id: Long): AssetTransactionEntity?
}
