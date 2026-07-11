package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "asset_transactions")
data class AssetTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val quantity: Double,
    val pricePerUnit: Double,
    val transactionDate: LocalDateTime,
    val transactionType: String, // buy or sell
    val fee: Double = 0.0,
    val note: String? = null,
    val currencyCode: String = "TRY"
)
