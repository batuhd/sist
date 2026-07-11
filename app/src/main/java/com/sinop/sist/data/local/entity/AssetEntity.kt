package com.sinop.sist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val assetType: String, // stock, fund, currency, gold
    val name: String? = null,
    val currencyCode: String = "TRY"
)
