package com.sinop.sist.domain.repository

import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetTransaction
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    suspend fun insertAsset(asset: Asset): Long
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(asset: Asset)
    suspend fun getAssetById(id: Long): Asset?
    suspend fun getAssetBySymbol(symbol: String): Asset?
    fun getAllAssets(): Flow<List<Asset>>
    fun getAssetsByType(type: AssetType): Flow<List<Asset>>

    suspend fun insertTransaction(transaction: AssetTransaction): Long
    suspend fun updateTransaction(transaction: AssetTransaction)
    suspend fun deleteTransaction(transaction: AssetTransaction)
    suspend fun getTransactionById(id: Long): AssetTransaction?
    fun getTransactionsByAssetId(assetId: Long): Flow<List<AssetTransaction>>

    fun getAssetsWithPrices(): Flow<List<AssetWithPrice>>
    fun getPortfolioSummary(): Flow<PortfolioSummary>
}
