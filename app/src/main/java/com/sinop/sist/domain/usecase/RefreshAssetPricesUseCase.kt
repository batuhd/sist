package com.sinop.sist.domain.usecase

import com.sinop.sist.data.remote.api.FinanceApiService
import com.sinop.sist.data.remote.provider.FvtFundPriceProvider
import com.sinop.sist.domain.model.Asset
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.PriceCache
import com.sinop.sist.domain.repository.PriceCacheRepository
import java.time.LocalDateTime

class RefreshAssetPricesUseCase(
    private val financeApiService: FinanceApiService,
    private val fvtFundPriceProvider: FvtFundPriceProvider,
    private val priceCacheRepository: PriceCacheRepository
) {

    suspend operator fun invoke(assets: List<Asset>): RefreshResult {
        val fetched = mutableMapOf<String, Double>()
        val failed = mutableListOf<String>()

        assets.forEach { asset ->
            when (asset.assetType) {
                AssetType.FUND -> fetchFromFvt(asset, fetched, failed)
                else -> fetchFromYahoo(asset, fetched, failed)
            }
        }

        return RefreshResult(fetched = fetched, failed = failed)
    }

    private suspend fun fetchFromFvt(
        asset: Asset,
        fetched: MutableMap<String, Double>,
        failed: MutableList<String>
    ) {
        try {
            val result = fvtFundPriceProvider.fetchFundPrice(asset.symbol)
            result.onSuccess { fund ->
                val price = fund.fiyat?.replace(",", "")?.toDoubleOrNull()
                if (price != null && price > 0) {
                    priceCacheRepository.savePrice(
                        PriceCache(
                            symbol = asset.symbol,
                            lastPrice = price,
                            lastUpdated = LocalDateTime.now(),
                            source = "fvt:${fund.fonKodu ?: asset.symbol}"
                        )
                    )
                    fetched[asset.symbol] = price
                } else {
                    failed.add(asset.symbol)
                }
            }.onFailure {
                failed.add(asset.symbol)
            }
        } catch (e: Exception) {
            failed.add(asset.symbol)
        }
    }

    private suspend fun fetchFromYahoo(
        asset: Asset,
        fetched: MutableMap<String, Double>,
        failed: MutableList<String>
    ) {
        val yahooSymbol = toYahooSymbol(asset)
        try {
            val response = financeApiService.getYahooChart(yahooSymbol)
            if (response.isSuccessful) {
                val price = response.body()
                    ?.chart
                    ?.result
                    ?.firstOrNull()
                    ?.meta
                    ?.regularMarketPrice

                if (price != null && price > 0) {
                    priceCacheRepository.savePrice(
                        PriceCache(
                            symbol = asset.symbol,
                            lastPrice = price,
                            lastUpdated = LocalDateTime.now(),
                            source = "yahoo:$yahooSymbol"
                        )
                    )
                    fetched[asset.symbol] = price
                } else {
                    failed.add(asset.symbol)
                }
            } else {
                failed.add(asset.symbol)
            }
        } catch (e: Exception) {
            failed.add(asset.symbol)
        }
    }

    private fun toYahooSymbol(asset: Asset): String {
        val symbol = asset.symbol.trim().uppercase()
        return when (asset.assetType) {
            AssetType.STOCK -> {
                if (symbol.endsWith(".IS")) symbol else "$symbol.IS"
            }
            AssetType.FUND -> {
                if (symbol.endsWith(".IS")) symbol else "$symbol.IS"
            }
            AssetType.CURRENCY -> {
                when {
                    symbol.contains("=") -> symbol
                    symbol.length == 6 -> symbol
                    symbol.length == 3 -> "${symbol}TRY=X"
                    else -> symbol
                }
            }
            AssetType.GOLD -> {
                when (symbol) {
                    "XAU", "GOLD", "ALTIN" -> "XAUTRYG=X"
                    else -> if (symbol.contains("=")) symbol else "XAUTRYG=X"
                }
            }
        }
    }
}

data class RefreshResult(
    val fetched: Map<String, Double>,
    val failed: List<String>
)
