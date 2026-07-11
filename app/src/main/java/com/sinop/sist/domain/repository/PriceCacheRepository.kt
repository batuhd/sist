package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.PriceCache
import kotlinx.coroutines.flow.Flow

interface PriceCacheRepository {
    suspend fun savePrice(price: PriceCache)
    suspend fun getPrice(symbol: String): PriceCache?
    fun getPrices(symbols: List<String>): Flow<List<PriceCache>>
    fun getAll(): Flow<List<PriceCache>>
}
