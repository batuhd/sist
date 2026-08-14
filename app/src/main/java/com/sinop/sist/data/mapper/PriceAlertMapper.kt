package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.PriceAlertEntity
import com.sinop.sist.domain.model.PriceAlert

fun PriceAlertEntity.toDomain(): PriceAlert {
    return PriceAlert(
        id = id,
        assetId = assetId,
        targetPrice = targetPrice,
        isAbove = isAbove,
        isActive = isActive,
        triggeredAt = triggeredAt,
        createdAt = createdAt
    )
}

fun PriceAlert.toEntity(): PriceAlertEntity {
    return PriceAlertEntity(
        id = id,
        assetId = assetId,
        targetPrice = targetPrice,
        isAbove = isAbove,
        isActive = isActive,
        triggeredAt = triggeredAt,
        createdAt = createdAt
    )
}
