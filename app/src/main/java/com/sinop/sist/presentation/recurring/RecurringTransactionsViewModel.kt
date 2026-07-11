package com.sinop.sist.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.domain.model.PaymentMethod
import com.sinop.sist.domain.model.RecurringTransaction
import com.sinop.sist.domain.model.RecurrencePeriod
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecurringTransactionsViewModel(
    private val recurringRepository: RecurringTransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringTransactionsState())
    val state: StateFlow<RecurringTransactionsState> = _state.asStateFlow()

    init {
        loadRecurring()
        loadCategories()
    }

    private fun loadRecurring() {
        recurringRepository.getAll()
            .onEach { list ->
                _state.value = _state.value.copy(recurring = list)
            }
            .launchIn(viewModelScope)
    }

    private fun loadCategories() {
        categoryRepository.getAll()
            .onEach { list ->
                _state.value = _state.value.copy(categories = list)
            }
            .launchIn(viewModelScope)
    }

    fun showDialog(recurring: RecurringTransaction? = null) {
        _state.value = _state.value.copy(
            showDialog = true,
            editingRecurring = recurring,
            dialogError = null
        )
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(
            showDialog = false,
            editingRecurring = null,
            dialogError = null
        )
    }

    fun saveRecurring(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        period: RecurrencePeriod,
        startDate: LocalDate,
        endDate: LocalDate?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            if (title.isBlank() || amount <= 0 || categoryId <= 0) {
                _state.value = _state.value.copy(dialogError = "Tüm alanları doğru doldurun.")
                return@launch
            }
            val recurring = RecurringTransaction(
                id = _state.value.editingRecurring?.id ?: 0,
                title = title.trim(),
                amount = amount,
                type = type,
                categoryId = categoryId,
                period = period,
                startDate = startDate,
                endDate = endDate,
                isActive = isActive,
                paymentMethod = PaymentMethod.CASH
            )
            if (_state.value.editingRecurring != null) {
                recurringRepository.update(recurring)
            } else {
                recurringRepository.insert(recurring)
            }
            dismissDialog()
        }
    }

    fun toggleActive(recurring: RecurringTransaction) {
        viewModelScope.launch {
            recurringRepository.update(recurring.copy(isActive = !recurring.isActive))
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            recurringRepository.delete(recurring)
        }
    }

    fun consumeDialogError() {
        _state.value = _state.value.copy(dialogError = null)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = com.sinop.sist.SistApplication.instance
                    ?: throw IllegalStateException("Application instance not available")
                return RecurringTransactionsViewModel(
                    recurringRepository = app.container.recurringTransactionRepository,
                    categoryRepository = app.container.categoryRepository
                ) as T
            }
        }
    }
}

data class RecurringTransactionsState(
    val recurring: List<RecurringTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showDialog: Boolean = false,
    val editingRecurring: RecurringTransaction? = null,
    val dialogError: String? = null
)
