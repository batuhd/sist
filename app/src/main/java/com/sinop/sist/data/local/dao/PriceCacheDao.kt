package com.sinop.sist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinop.sist.data.local.entity.PriceCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(price: PriceCacheEntity)

    @Query("SELECT * FROM price_cache WHERE symbol = :symbol")
    suspend fun getBySymbol(symbol: String): PriceCacheEntity?

    @Query("SELECT * FROM price_cache WHERE symbol IN (:symbols)")
    fun getBySymbols(symbols: List<String>): Flow<List<PriceCacheEntity>>

    @Query("SELECT * FROM price_cache ORDER BY symbol ASC")
    fun getAll(): Flow<List<PriceCacheEntity>>
}
