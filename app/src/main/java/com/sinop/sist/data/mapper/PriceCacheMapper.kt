package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.PriceCacheEntity
import com.sinop.sist.domain.model.PriceCache

fun PriceCacheEntity.toDomain(): PriceCache = PriceCache(
    symbol = symbol,
    lastPrice = lastPrice,
    lastUpdated = lastUpdated,
    source = source
)

fun PriceCache.toEntity(): PriceCacheEntity = PriceCacheEntity(
    symbol = symbol,
    lastPrice = lastPrice,
    lastUpdated = lastUpdated,
    source = source
)
