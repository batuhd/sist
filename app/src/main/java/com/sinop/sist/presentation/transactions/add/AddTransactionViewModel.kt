package com.sinop.sist.presentation.transactions.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Account
import com.sinop.sist.domain.model.AccountType
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.PaymentMethod
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.AccountRepository
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private val _event = MutableStateFlow<AddTransactionEvent?>(null)
    val event: StateFlow<AddTransactionEvent?> = _event.asStateFlow()

    init {
        loadCategories()
        loadAccounts()
        transactionId?.let { loadTransaction(it) }
    }

    private fun loadCategories() {
        categoryRepository.getAll().onEach { categories ->
            _state.value = _state.value.copy(
                categories = categories,
                categoryId = _state.value.categoryId ?: categories.firstOrNull {
                    it.type == com.sinop.sist.domain.model.CategoryType.EXPENSE ||
                            it.type == com.sinop.sist.domain.model.CategoryType.BOTH
                }?.id
            )
        }.launchIn(viewModelScope)
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.seedDefaultAccounts()
            _state.value = _state.value.copy(accounts = accounts)
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.getById(id)?.let { transaction ->
                _state.value = _state.value.copy(
                    amount = transaction.amount.toString(),
                    type = transaction.type,
                    categoryId = transaction.categoryId,
                    accountId = transaction.accountId,
                    date = transaction.date.toLocalDate(),
                    time = transaction.date.toLocalTime(),
                    note = transaction.note ?: "",
                    tags = transaction.tags.joinToString(","),
                    paymentMethod = transaction.paymentMethod,
                    isEditing = true
                )
            }
        }
    }

    fun onAmountChange(amount: String) {
        _state.value = _state.value.copy(amount = amount)
    }

    fun onTypeChange(type: TransactionType) {
        _state.value = _state.value.copy(type = type)
    }

    fun onCategoryChange(categoryId: Long) {
        _state.value = _state.value.copy(categoryId = categoryId)
    }

    fun onDateChange(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun onTimeChange(time: LocalTime) {
        _state.value = _state.value.copy(time = time)
    }

    fun onNoteChange(note: String) {
        _state.value = _state.value.copy(note = note)
    }

    fun onTagsChange(tags: String) {
        _state.value = _state.value.copy(tags = tags)
    }

    fun onPaymentMethodChange(method: PaymentMethod?) {
        val accounts = _state.value.accounts
        val accountId = when (method) {
            PaymentMethod.CASH -> accounts.find { it.type == AccountType.CASH }?.id
            PaymentMethod.BANK -> {
                val bankAccounts = accounts.filter { it.type == AccountType.BANK }
                if (bankAccounts.size == 1) bankAccounts.first().id else _state.value.accountId
            }
            else -> _state.value.accountId
        }
        _state.value = _state.value.copy(paymentMethod = method, accountId = accountId)
    }

    fun onAccountChange(accountId: Long?) {
        _state.value = _state.value.copy(accountId = accountId)
    }

    fun saveTransaction() {
        val currentState = _state.value
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = currentState.copy(error = "Geçerli bir tutar girin")
            return
        }
        if (currentState.categoryId == null) {
            _state.value = currentState.copy(error = "Kategori seçin")
            return
        }

        val accountId = currentState.accountId ?: when (currentState.paymentMethod) {
            PaymentMethod.CASH -> currentState.accounts.find { it.type == AccountType.CASH }?.id
            PaymentMethod.BANK -> currentState.accounts.find { it.type == AccountType.BANK }?.id
            else -> currentState.accounts.firstOrNull()?.id
        }

        val transaction = Transaction(
            id = transactionId ?: 0,
            amount = amount,
            type = currentState.type,
            categoryId = currentState.categoryId,
            accountId = accountId,
            date = LocalDateTime.of(currentState.date, currentState.time),
            note = currentState.note.takeIf { it.isNotBlank() },
            tags = currentState.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            paymentMethod = currentState.paymentMethod
        )

        viewModelScope.launch {
            if (transactionId != null) {
                transactionRepository.update(transaction)
            } else {
                transactionRepository.insert(transaction)
            }
            _event.value = AddTransactionEvent.Saved
        }
    }

    fun consumeEvent() {
        _event.value = null
    }

    companion object {
        fun factory(transactionId: Long? = null): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return AddTransactionViewModel(
                    app.container.transactionRepository,
                    app.container.categoryRepository,
                    app.container.accountRepository,
                    transactionId
                ) as T
            }
        }
    }
}

data class AddTransactionState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val note: String = "",
    val tags: String = "",
    val paymentMethod: PaymentMethod? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isEditing: Boolean = false,
    val error: String? = null
)

sealed class AddTransactionEvent {
    data object Saved : AddTransactionEvent()
}
