package com.sinop.sist.presentation.widget

import android.content.Context
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.BudgetWithSpending
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.PriceCache
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Widget'lar icin veri hazirlayan tek nokta.
 * Yalnizca mevcut repository verilerinden turetilir; veri/schema degistirmez.
 */
object WidgetDataProvider {

    private const val STALE_THRESHOLD_HOURS = 6L

    suspend fun getAccountsWithBalances(app: SistApplication): List<Account> {
        val accounts = app.container.accountRepository.getAll().first()
        val transactions = app.container.transactionRepository.getAll().first()

        return accounts.map { account ->
            val balance = transactions.sumOf { transaction ->
                when {
                    transaction.type == TransactionType.TRANSFER && transaction.accountId == account.id -> -transaction.amount
                    transaction.type == TransactionType.TRANSFER && transaction.toAccountId == account.id -> transaction.amount
                    transaction.type == TransactionType.INCOME && transaction.accountId == account.id -> transaction.amount
                    transaction.type == TransactionType.EXPENSE && transaction.accountId == account.id -> -transaction.amount
                    else -> 0.0
                }
            }
            account.copy(balance = balance)
        }
    }

    suspend fun getPortfolioSummary(app: SistApplication): PortfolioSummary {
        return app.container.assetRepository.getPortfolioSummary().first()
    }

    suspend fun getAssetsWithHoldings(app: SistApplication): List<AssetWithPrice> {
        val assets = app.container.assetRepository.getAssetsWithPrices().first()
        return assets.sortedByDescending { it.currentValue ?: 0.0 }
    }

    suspend fun getTotalNetWorth(app: SistApplication): Double {
        val accounts = getAccountsWithBalances(app)
        val portfolioSummary = getPortfolioSummary(app)
        val accountBalance = accounts
            .filter { it.type != AccountType.INVESTMENT }
            .sumOf { it.balance }
        val portfolioValue = portfolioSummary.totalValue ?: 0.0
        return accountBalance + portfolioValue
    }

    /**
     * En hareketli varliklar: mutlak kar/zarar yuzdesi en yuksek olanlar.
     * "Bugun portfoyumde asil hareket eden ne" sorusunu cevaplar.
     */
    suspend fun getTopMovers(app: SistApplication, limit: Int): List<AssetWithPrice> {
        return getAssetsWithHoldings(app)
            .filter { it.profitLossPercent != null }
            .sortedByDescending { kotlin.math.abs(it.profitLossPercent ?: 0.0) }
            .take(limit)
    }

    fun isStale(assets: List<AssetWithPrice>): Boolean {
        val newest = assets.mapNotNull { it.lastUpdated }.maxOrNull() ?: return false
        return newest.isBefore(LocalDateTime.now().minusHours(STALE_THRESHOLD_HOURS))
    }

    /**
     * Bütçe anlık görüntüsü: genel bütçe + kategori bütçeleri (isimleriyle).
     */
    data class BudgetSnapshot(
        val general: BudgetWithSpending?,
        val categoryBudgets: List<Pair<String, BudgetWithSpending>>,
        val hasBudgets: Boolean
    )

    suspend fun getBudgetSnapshot(app: SistApplication): BudgetSnapshot {
        val budgets = app.container.budgetRepository.getByMonth(YearMonth.now()).first()
        if (budgets.isEmpty()) return BudgetSnapshot(null, emptyList(), hasBudgets = false)

        val categories = app.container.categoryRepository.getAll().first().associateBy { it.id }
        val general = budgets.find { it.budget.categoryId == null }
        val categoryBudgets = budgets
            .filter { it.budget.categoryId != null }
            .sortedByDescending { it.spent }
            .mapNotNull { budget ->
                val category = categories[budget.budget.categoryId] ?: return@mapNotNull null
                category.name to budget
            }
        return BudgetSnapshot(general, categoryBudgets, hasBudgets = true)
    }

    /**
     * En sık kullanılan gider kategorileri (işlem sayısına göre).
     * Hızlı Ekle widget'ındaki kısayol çipleri için.
     */
    suspend fun getTopExpenseCategories(app: SistApplication, limit: Int): List<Category> {
        val transactions = app.container.transactionRepository.getAll().first()
        val categories = app.container.categoryRepository.getAll().first().associateBy { it.id }
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupingBy { it.categoryId }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .mapNotNull { categories[it.key] }
            .take(limit)
    }

    data class NetWorthBreakdown(
        val total: Double,
        val accounts: Double,
        val portfolio: Double,
        val accountList: List<Account>
    )

    suspend fun getNetWorthBreakdown(app: SistApplication): NetWorthBreakdown {
        val accounts = getAccountsWithBalances(app)
        val portfolioSummary = getPortfolioSummary(app)
        val nonInvestment = accounts.filter { it.type != AccountType.INVESTMENT }
        val accountTotal = nonInvestment.sumOf { it.balance }
        val portfolioTotal = portfolioSummary.totalValue ?: 0.0
        return NetWorthBreakdown(
            total = accountTotal + portfolioTotal,
            accounts = accountTotal,
            portfolio = portfolioTotal,
            accountList = nonInvestment.sortedByDescending { it.balance }
        )
    }

    /**
     * Portföy dağılımı: varlık türü bazlı paylar (stacked bar için).
     */
    data class DistributionPart(
        val assetType: AssetType,
        val label: String,
        val value: Double,
        val share: Double
    )

    data class DistributionAssetRow(
        val symbol: String,
        val assetType: AssetType,
        val typeLabel: String,
        val value: Double,
        val share: Double
    )

    data class DistributionSnapshot(
        val parts: List<DistributionPart>,
        val totalValue: Double,
        val maxAssetShare: Double,
        val assetRows: List<DistributionAssetRow>
    )

    suspend fun getDistributionSnapshot(app: SistApplication): DistributionSnapshot {
        val summary = getPortfolioSummary(app)
        val total = summary.totalValue ?: 0.0
        // assetDistribution degerleri pay (0..1) olarak gelir; mutlak tutar pay * toplamdir.
        val parts = summary.assetDistribution
            .entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .map { (type, share) ->
                DistributionPart(
                    assetType = type,
                    label = assetTypeLabel(type),
                    value = share * total,
                    share = share.coerceIn(0.0, 1.0)
                )
            }

        val assets = getAssetsWithHoldings(app)
        val maxAssetShare = if (total > 0) {
            (assets.maxOfOrNull { it.currentValue ?: 0.0 } ?: 0.0) / total
        } else 0.0

        val assetRows = assets
            .sortedByDescending { it.currentValue ?: 0.0 }
            .take(5)
            .map { asset ->
                val value = asset.currentValue ?: 0.0
                DistributionAssetRow(
                    symbol = asset.asset.symbol,
                    assetType = asset.asset.assetType,
                    typeLabel = assetTypeLabel(asset.asset.assetType),
                    value = value,
                    share = if (total > 0) value / total else 0.0
                )
            }

        return DistributionSnapshot(parts, total, maxAssetShare, assetRows)
    }

    private fun assetTypeLabel(type: AssetType): String = when (type) {
        AssetType.STOCK -> "Hisse"
        AssetType.FUND -> "Fon"
        AssetType.CURRENCY -> "Döviz"
        AssetType.GOLD -> "Altın"
    }

    fun assetTypeColorKey(type: AssetType): Int = when (type) {
        AssetType.STOCK -> com.sinop.sist.R.color.widget_category_blue
        AssetType.FUND -> com.sinop.sist.R.color.widget_category_purple
        AssetType.CURRENCY -> com.sinop.sist.R.color.widget_category_orange
        AssetType.GOLD -> com.sinop.sist.R.color.widget_category_yellow
    }

    // ------------------------------------------------------------------
    // Takip Listesi widget'i
    // ------------------------------------------------------------------

    private const val WATCHLIST_PREFS = "sist_watchlist_prefs"
    private const val MAX_WATCH_SYMBOLS = 5

    fun watchlistPrefsKey(widgetId: Int): String = "symbols_$widgetId"

    fun getWatchlistSymbols(context: Context, widgetId: Int): List<String> {
        val raw = context.getSharedPreferences(WATCHLIST_PREFS, Context.MODE_PRIVATE)
            .getString(watchlistPrefsKey(widgetId), null) ?: return emptyList()
        return raw.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }.take(MAX_WATCH_SYMBOLS)
    }

    fun saveWatchlistSymbols(context: Context, widgetId: Int, symbols: List<String>) {
        val clean = symbols.map { it.trim().uppercase() }.filter { it.isNotBlank() }.take(MAX_WATCH_SYMBOLS)
        val editor = context.getSharedPreferences(WATCHLIST_PREFS, Context.MODE_PRIVATE).edit()
        if (clean.isEmpty()) {
            editor.remove(watchlistPrefsKey(widgetId))
        } else {
            editor.putString(watchlistPrefsKey(widgetId), clean.joinToString(","))
        }
        editor.apply()
    }

    data class WatchlistRow(
        val symbol: String,
        val price: Double?,
        val profitLossPercent: Double?,
        val source: String?,
        val updated: LocalDateTime?
    )

    /**
     * Takip listesi satırlarını üretir.
     * Kullanıcı sembol seçmemişse portföyün en hareketli varlıklarını gösterir.
     * Serbest semboller için mevcut Yahoo sağlayıcısıyla fiyat çekilir,
     * başarısız olursa son bilinen fiyat kullanılır.
     */
    suspend fun getWatchlistRows(context: Context, app: SistApplication, widgetId: Int): List<WatchlistRow> {
        val assets = getAssetsWithHoldings(app)
        val bySymbol = assets.associateBy { it.asset.symbol.trim().uppercase() }

        val configured = getWatchlistSymbols(context, widgetId)
        val symbols = if (configured.isEmpty()) {
            getTopMovers(app, 4).map { it.asset.symbol.trim().uppercase() }
        } else {
            configured
        }

        return symbols.map { symbol ->
            val holding = bySymbol[symbol]
            if (holding != null) {
                WatchlistRow(
                    symbol = holding.asset.symbol,
                    price = holding.currentPrice,
                    profitLossPercent = holding.profitLossPercent,
                    source = holding.priceSource,
                    updated = holding.lastUpdated
                )
            } else {
                val cache = fetchSymbolPrice(app, symbol)
                WatchlistRow(
                    symbol = symbol,
                    price = cache?.lastPrice,
                    profitLossPercent = null,
                    source = cache?.source,
                    updated = cache?.lastUpdated
                )
            }
        }
    }

    private suspend fun fetchSymbolPrice(app: SistApplication, symbol: String): PriceCache? {
        val yahooSymbol = toYahooSymbol(symbol)
        val fresh = withTimeoutOrNull(8_000) {
            runCatching {
                val response = app.container.financeApiService.getYahooChart(yahooSymbol)
                if (response.isSuccessful) {
                    response.body()?.chart?.result?.firstOrNull()?.meta?.regularMarketPrice
                } else null
            }.getOrNull()
        }
        if (fresh != null && fresh > 0) {
            val cache = PriceCache(
                symbol = symbol,
                lastPrice = fresh,
                lastUpdated = LocalDateTime.now(),
                source = "yahoo:$yahooSymbol"
            )
            app.container.priceCacheRepository.savePrice(cache)
            return cache
        }
        return app.container.priceCacheRepository.getPrice(symbol)
    }

    private fun toYahooSymbol(symbol: String): String {
        val s = symbol.trim().uppercase()
        return when {
            s.contains("=") -> s
            s.endsWith(".IS") -> s
            s.length <= 6 && s.all { it.isLetterOrDigit() } -> "$s.IS"
            else -> s
        }
    }
}
