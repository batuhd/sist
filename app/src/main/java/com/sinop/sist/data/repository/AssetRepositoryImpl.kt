package com.sinop.sist.data.repository

import com.sinop.sist.data.local.dao.AssetDao
import com.sinop.sist.data.local.dao.AssetTransactionDao
import com.sinop.sist.data.local.dao.PriceCacheDao
import com.sinop.sist.data.mapper.toDomain
import com.sinop.sist.data.mapper.toEntity
import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AssetRepositoryImpl(
    private val assetDao: AssetDao,
    private val assetTransactionDao: AssetTransactionDao,
    private val priceCacheDao: PriceCacheDao
) : AssetRepository {

    override suspend fun insertAsset(asset: Asset): Long {
        return assetDao.insert(asset.toEntity())
    }

    override suspend fun updateAsset(asset: Asset) {
        assetDao.update(asset.toEntity())
    }

    override suspend fun deleteAsset(asset: Asset) {
        assetDao.delete(asset.toEntity())
    }

    override suspend fun getAssetById(id: Long): Asset? {
        return assetDao.getById(id)?.toDomain()
    }

    override suspend fun getAssetBySymbol(symbol: String): Asset? {
        return assetDao.getBySymbol(symbol)?.toDomain()
    }

    override fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getAssetsByType(type: AssetType): Flow<List<Asset>> {
        return assetDao.getByType(type.name.lowercase()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTransaction(transaction: AssetTransaction): Long {
        return assetTransactionDao.insert(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: AssetTransaction) {
        assetTransactionDao.update(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: AssetTransaction) {
        assetTransactionDao.delete(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: Long): AssetTransaction? {
        return assetTransactionDao.getById(id)?.toDomain()
    }

    override fun getTransactionsByAssetId(assetId: Long): Flow<List<AssetTransaction>> {
        return assetTransactionDao.getByAssetId(assetId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAssetsWithPrices(): Flow<List<AssetWithPrice>> {
        return combine(
            assetDao.getAll(),
            assetTransactionDao.getAll(),
            priceCacheDao.getAll()
        ) { assets, transactions, prices ->
            val priceMap = prices.associateBy { it.symbol }
            assets.map { asset ->
                val assetTransactions = transactions.filter { it.assetId == asset.id }
                val totalBuyQuantity = assetTransactions
                    .filter { it.transactionType == "buy" }
                    .sumOf { it.quantity }
                val totalBuyCost = assetTransactions
                    .filter { it.transactionType == "buy" }
                    .sumOf { it.quantity * it.pricePerUnit + it.fee }
                val totalSellQuantity = assetTransactions
                    .filter { it.transactionType == "sell" }
                    .sumOf { it.quantity }

                val totalQuantity = totalBuyQuantity - totalSellQuantity
                val avgCost = if (totalBuyQuantity > 0) totalBuyCost / totalBuyQuantity else 0.0
                val totalCost = avgCost * totalQuantity
                val priceCache = priceMap[asset.symbol]
                val currentPrice = priceCache?.lastPrice
                val currentValue = currentPrice?.let { it * totalQuantity }
                val profitLoss = currentValue?.minus(totalCost)
                val profitLossPercent = if (totalCost > 0) profitLoss?.div(totalCost)?.times(100) else null

                AssetWithPrice(
                    asset = asset.toDomain(),
                    averageCost = avgCost,
                    currentPrice = currentPrice,
                    totalQuantity = totalQuantity,
                    totalCost = totalCost,
                    currentValue = currentValue,
                    profitLoss = profitLoss,
                    profitLossPercent = profitLossPercent,
                    priceSource = priceCache?.source
                )
            }
        }
    }

    override fun getPortfolioSummary(): Flow<PortfolioSummary> {
        return getAssetsWithPrices().map { assets ->
            val totalCost = assets.sumOf { it.totalCost }
            val totalValue = assets.mapNotNull { it.currentValue }.sum()
            val profitLoss = if (totalValue > 0) totalValue - totalCost else null
            val profitLossPercent = if (totalCost > 0) profitLoss?.div(totalCost)?.times(100) else null

            val distribution = assets.groupBy { it.asset.assetType }
                .mapValues { entry -> entry.value.sumOf { it.currentValue ?: 0.0 } }
                .mapValues { if (totalValue > 0) it.value / totalValue else 0.0 }

            PortfolioSummary(
                totalCost = totalCost,
                totalValue = totalValue,
                totalProfitLoss = profitLoss,
                totalProfitLossPercent = profitLossPercent,
                assetDistribution = distribution
            )
        }
    }
}
