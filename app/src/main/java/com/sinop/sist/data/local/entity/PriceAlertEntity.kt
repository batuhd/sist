package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val targetPrice: Double,
    val isAbove: Boolean,
    val isActive: Boolean = true,
    val triggeredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
