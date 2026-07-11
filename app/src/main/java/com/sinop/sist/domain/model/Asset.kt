package com.sinop.sist.domain.model

data class Asset(
    val id: Long = 0,
    val symbol: String,
    val assetType: AssetType,
    val name: String? = null,
    val currencyCode: String = "TRY"
)

enum class AssetType { STOCK, FUND, CURRENCY, GOLD }

data class AssetTransaction(
    val id: Long = 0,
    val assetId: Long,
    val quantity: Double,
    val pricePerUnit: Double,
    val transactionDate: java.time.LocalDateTime,
    val transactionType: AssetTransactionType,
    val fee: Double = 0.0,
    val note: String? = null,
    val currencyCode: String = "TRY"
)

enum class AssetTransactionType { BUY, SELL }

data class AssetWithPrice(
    val asset: Asset,
    val averageCost: Double,
    val currentPrice: Double?,
    val totalQuantity: Double,
    val totalCost: Double,
    val currentValue: Double?,
    val profitLoss: Double?,
    val profitLossPercent: Double?,
    val priceSource: String? = null
)

data class PortfolioSummary(
    val totalCost: Double,
    val totalValue: Double?,
    val totalProfitLoss: Double?,
    val totalProfitLossPercent: Double?,
    val assetDistribution: Map<AssetType, Double>
)
