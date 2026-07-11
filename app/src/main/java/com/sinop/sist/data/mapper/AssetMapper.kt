package com.sinop.sist.data.mapper

import com.sinop.sist.data.local.entity.AssetEntity
import com.sinop.sist.data.local.entity.AssetTransactionEntity
import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetTransactionType
import com.sinop.sist.domain.model.AssetType

fun AssetEntity.toDomain(): Asset = Asset(
    id = id,
    symbol = symbol,
    assetType = AssetType.valueOf(assetType.uppercase()),
    name = name,
    currencyCode = currencyCode
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id = id,
    symbol = symbol,
    assetType = assetType.name.lowercase(),
    name = name,
    currencyCode = currencyCode
)

fun AssetTransactionEntity.toDomain(): AssetTransaction = AssetTransaction(
    id = id,
    assetId = assetId,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    transactionDate = transactionDate,
    transactionType = AssetTransactionType.valueOf(transactionType.uppercase()),
    fee = fee,
    note = note,
    currencyCode = currencyCode
)

fun AssetTransaction.toEntity(): AssetTransactionEntity = AssetTransactionEntity(
    id = id,
    assetId = assetId,
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    transactionDate = transactionDate,
    transactionType = transactionType.name.lowercase(),
    fee = fee,
    note = note,
    currencyCode = currencyCode
)
