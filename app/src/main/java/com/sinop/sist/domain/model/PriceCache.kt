package com.sinop.sist.domain.model

import java.time.LocalDateTime

data class PriceCache(
    val symbol: String,
    val lastPrice: Double,
    val lastUpdated: LocalDateTime,
    val source: String? = null
)
