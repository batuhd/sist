package com.sinop.sist.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.domain.model.Budget
import com.sinop.sist.domain.model.BudgetWithSpending
import com.sinop.sist.domain.model.Category
import com.sinop.sist.domain.model.CategoryType
import com.sinop.sist.domain.repository.BudgetRepository
import com.sinop.sist.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.YearMonth

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetsState())
    val state: StateFlow<BudgetsState> = _state.asStateFlow()

    init {
        loadCategories()
        loadBudgets()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAll().collect { categories ->
                _state.value = _state.value.copy(
                    categories = categories.filter {
                        it.type == CategoryType.EXPENSE || it.type == CategoryType.BOTH
                    }
                )
            }
        }
    }

    private fun loadBudgets() {
        combine(
            _state,
            budgetRepository.getByMonth(_state.value.month)
        ) { state, budgets ->
            state.copy(budgets = budgets)
        }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun setMonth(month: YearMonth) {
        if (_state.value.month == month) return
        _state.value = _state.value.copy(month = month)
        loadBudgets()
    }

    fun showAddDialog(budget: BudgetWithSpending? = null) {
        _state.value = _state.value.copy(
            showDialog = true,
            editingBudget = budget?.budget
        )
    }

    fun dismissDialog() {
        _state.value = _state.value.copy(
            showDialog = false,
            editingBudget = null,
            dialogError = null
        )
    }

    fun saveBudget(categoryId: Long?, limit: Double) {
        viewModelScope.launch {
            val month = _state.value.month
            val existing = budgetRepository.getByCategoryAndMonth(categoryId, month)
            if (existing != null && (_state.value.editingBudget == null || existing.id != _state.value.editingBudget?.id)) {
                _state.value = _state.value.copy(dialogError = "Bu kategori için bu ay zaten bütçe tanımlı.")
                return@launch
            }
            val budget = Budget(
                id = _state.value.editingBudget?.id ?: 0,
                categoryId = categoryId,
                monthlyLimit = limit,
                month = month
            )
            if (_state.value.editingBudget != null) {
                budgetRepository.update(budget)
            } else {
                budgetRepository.insert(budget)
            }
            dismissDialog()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.delete(budget)
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
                return BudgetsViewModel(
                    budgetRepository = app.container.budgetRepository,
                    categoryRepository = app.container.categoryRepository
                ) as T
            }
        }
    }
}

data class BudgetsState(
    val month: YearMonth = YearMonth.now(),
    val budgets: List<BudgetWithSpending> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showDialog: Boolean = false,
    val editingBudget: Budget? = null,
    val dialogError: String? = null
)
