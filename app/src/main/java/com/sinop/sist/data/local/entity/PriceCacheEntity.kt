package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "price_cache")
data class PriceCacheEntity(
    @PrimaryKey
    val symbol: String,
    val lastPrice: Double,
    val lastUpdated: LocalDateTime,
    val source: String? = null
)
