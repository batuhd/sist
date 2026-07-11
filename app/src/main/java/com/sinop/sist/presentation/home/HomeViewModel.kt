package com.sinop.sist.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.AccountRepository
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategories()
            accountRepository.seedDefaultAccounts()
        }

        val month = YearMonth.now()
        val start = month.atDay(1).atStartOfDay()
        val end = month.atEndOfMonth().atTime(23, 59, 59)

        combine(
            transactionRepository.getSumByTypeAndPeriod(TransactionType.INCOME, start, end),
            transactionRepository.getSumByTypeAndPeriod(TransactionType.EXPENSE, start, end),
            transactionRepository.getBetween(start, end),
            accountRepository.getAll(),
            assetRepository.getPortfolioSummary()
        ) { income, expense, transactions, accounts, portfolio ->
            val visibleAccounts = accounts.filter { it.type != AccountType.INVESTMENT }
            val accountBalances = visibleAccounts.map { account ->
                val balance = transactions
                    .filter { it.accountId == account.id }
                    .sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
                account.copy(balance = balance)
            }
            val portfolioValue = portfolio.totalValue ?: 0.0
            val totalBalance = accountBalances.sumOf { it.balance } + portfolioValue
            _state.value = _state.value.copy(
                monthlyIncome = income,
                monthlyExpense = expense,
                monthlyBalance = income - expense,
                totalBalance = totalBalance,
                portfolioValue = portfolioValue,
                accounts = accountBalances
            )
        }.launchIn(viewModelScope)
    }

    fun toggleAccountsExpanded() {
        _state.value = _state.value.copy(accountsExpanded = !_state.value.accountsExpanded)
    }

    fun addBankAccount(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return@launch
            val account = Account(
                name = trimmed,
                type = AccountType.BANK,
                iconName = "account_balance",
                colorHex = "#2196F3",
                isDefault = false
            )
            accountRepository.insert(account)
        }
    }

    fun showAddAccountDialog(show: Boolean) {
        _state.value = _state.value.copy(showAddAccountDialog = show)
    }

    fun showDeleteAccountDialog(account: Account?) {
        _state.value = _state.value.copy(
            accountToDelete = account,
            deleteAccountError = null
        )
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            if (account.isDefault) {
                _state.value = _state.value.copy(
                    deleteAccountError = "Varsayılan hesaplar silinemez"
                )
                return@launch
            }
            val transactions = transactionRepository.getByAccount(account.id).first()
            if (transactions.isNotEmpty()) {
                _state.value = _state.value.copy(
                    deleteAccountError = "Bu hesaba ait işlemler var. Önce işlemleri silin veya başka hesaba taşıyın."
                )
                return@launch
            }
            accountRepository.delete(account)
            showDeleteAccountDialog(null)
        }
    }

    fun consumeDeleteAccountError() {
        _state.value = _state.value.copy(deleteAccountError = null)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return HomeViewModel(
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                    app.container.accountRepository,
                    app.container.assetRepository
                ) as T
            }
        }
    }
}

data class HomeState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyBalance: Double = 0.0,
    val totalBalance: Double = 0.0,
    val portfolioValue: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val accountsExpanded: Boolean = true,
    val showAddAccountDialog: Boolean = false,
    val accountToDelete: Account? = null,
    val deleteAccountError: String? = null
)
