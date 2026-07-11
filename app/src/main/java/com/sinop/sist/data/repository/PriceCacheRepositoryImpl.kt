package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.PriceCacheDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.PriceCache
import com.sinop.sist.domain.repository.PriceCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PriceCacheRepositoryImpl(
    private val priceCacheDao: PriceCacheDao
) : PriceCacheRepository {

    override suspend fun savePrice(price: PriceCache) {
        priceCacheDao.insert(price.toEntity())
    }

    override suspend fun getPrice(symbol: String): PriceCache? {
        return priceCacheDao.getBySymbol(symbol)?.toDomain()
    }

    override fun getPrices(symbols: List<String>): Flow<List<PriceCache>> {
        return priceCacheDao.getBySymbols(symbols).map { list -> list.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<PriceCache>> {
        return priceCacheDao.getAll().map { list -> list.map { it.toDomain() } }
    }
}
