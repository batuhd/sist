package com.sinop.sist.presentation.widget

import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.AssetWithPrice
import com.sinop.sist.domain.model.PortfolioSummary
import com.sinop.sist.domain.model.TransactionType
import kotlinx.coroutines.flow.first

object WidgetDataProvider {

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
}
