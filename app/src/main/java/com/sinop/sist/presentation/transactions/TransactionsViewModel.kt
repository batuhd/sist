package com.sinop.sist.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.seedDefaultCategories()
        }

        val month = YearMonth.now()
        val start = month.atDay(1).atStartOfDay()
        val end = month.atEndOfMonth().atTime(23, 59, 59)

        combine(
            transactionRepository.getBetween(start, end),
            categoryRepository.getAll(),
            transactionRepository.getSumByTypeAndPeriod(TransactionType.INCOME, start, end),
            transactionRepository.getSumByTypeAndPeriod(TransactionType.EXPENSE, start, end)
        ) { transactions, categories, income, expense ->
            updateState(transactions, categories, income, expense)
        }.launchIn(viewModelScope)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _state.value = _state.value.copy(selectedMonth = yearMonth)
        val start = yearMonth.atDay(1).atStartOfDay()
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59)

        combine(
            transactionRepository.getBetween(start, end),
            categoryRepository.getAll(),
            transactionRepository.getSumByTypeAndPeriod(TransactionType.INCOME, start, end),
            transactionRepository.getSumByTypeAndPeriod(TransactionType.EXPENSE, start, end)
        ) { transactions, categories, income, expense ->
            updateState(transactions, categories, income, expense)
        }.launchIn(viewModelScope)
    }

    fun setSortOrder(order: TransactionSortOrder) {
        _state.value = _state.value.copy(sortOrder = order)
    }

    fun setViewMode(mode: TransactionViewMode) {
        _state.value = _state.value.copy(viewMode = mode)
    }

    private fun updateState(
        transactions: List<Transaction>,
        categories: List<Category>,
        income: Double,
        expense: Double
    ) {
        val sorted = sortTransactions(transactions, _state.value.sortOrder)
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, txs) ->
                val category = categories.find { it.id == categoryId }
                category?.let { CategoryExpense(it, txs.sumOf { t -> t.amount }) }
            }
            .sortedByDescending { it.amount }

        _state.value = _state.value.copy(
            transactions = sorted,
            categories = categories,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            expensesByCategory = expensesByCategory
        )
    }

    private fun sortTransactions(
        transactions: List<Transaction>,
        order: TransactionSortOrder
    ): List<Transaction> {
        return when (order) {
            TransactionSortOrder.DATE_DESC -> transactions.sortedByDescending { it.date }
            TransactionSortOrder.DATE_ASC -> transactions.sortedBy { it.date }
            TransactionSortOrder.AMOUNT_DESC -> transactions.sortedByDescending { it.amount }
            TransactionSortOrder.AMOUNT_ASC -> transactions.sortedBy { it.amount }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return TransactionsViewModel(
                    app.container.transactionRepository,
                    app.container.categoryRepository
                ) as T
            }
        }
    }
}

enum class TransactionSortOrder {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC
}

enum class TransactionViewMode {
    LIST, CHART
}

data class CategoryExpense(
    val category: Category,
    val amount: Double
)

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val selectedMonth: YearMonth = YearMonth.now(),
    val sortOrder: TransactionSortOrder = TransactionSortOrder.DATE_DESC,
    val viewMode: TransactionViewMode = TransactionViewMode.LIST,
    val expensesByCategory: List<CategoryExpense> = emptyList()
)
