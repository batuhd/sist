package com.sinop.sist.domain.model

import java.time.LocalDateTime

data class PriceAlert(
    val id: Long = 0,
    val assetId: Long,
    val targetPrice: Double,
    val isAbove: Boolean,
    val isActive: Boolean = true,
    val triggeredAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
