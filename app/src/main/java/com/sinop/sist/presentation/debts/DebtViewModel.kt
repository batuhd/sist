package com.sinop.sist.presentation.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Debt
import com.sinop.sist.domain.model.DebtDirection
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.DebtRepository
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class DebtViewModel(
    private val debtRepository: DebtRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DebtState())
    val state: StateFlow<DebtState> = _state.asStateFlow()

    init {
        loadDebts()
    }

    private fun loadDebts() {
        debtRepository.getAll().onEach { debts ->
            val totalGiven = debts.filter { it.direction == DebtDirection.GIVEN && !it.isPaid }.sumOf { it.amount }
            val totalReceived = debts.filter { it.direction == DebtDirection.RECEIVED && !it.isPaid }.sumOf { it.amount }
            _state.value = _state.value.copy(
                debts = debts,
                totalGiven = totalGiven,
                totalReceived = totalReceived
            )
        }.launchIn(viewModelScope)
    }

    fun addDebt(
        personName: String,
        amount: Double,
        direction: DebtDirection,
        dueDate: LocalDate?,
        note: String?
    ) {
        viewModelScope.launch {
            val categories = categoryRepository.seedDefaultCategories()
            val categoryName = if (direction == DebtDirection.GIVEN) "Borç/Taksit" else "Borç Geri Alma"
            val categoryId = categories.find { it.name == categoryName }?.id ?: return@launch

            val debt = Debt(
                personName = personName,
                amount = amount,
                direction = direction,
                dueDate = dueDate,
                note = note
            )
            val debtId = debtRepository.insert(debt)

            val transaction = Transaction(
                amount = amount,
                type = if (direction == DebtDirection.GIVEN) TransactionType.EXPENSE else TransactionType.INCOME,
                categoryId = categoryId,
                date = LocalDateTime.now(),
                note = if (direction == DebtDirection.GIVEN) "$personName adlı kişiye borç verildi" else "$personName adlı kişiden borç alındı",
                paymentMethod = null,
                currencyCode = "TRY"
            )
            transactionRepository.insert(transaction)
        }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.update(debt)
        }
    }

    fun markAsPaid(debt: Debt) {
        viewModelScope.launch {
            val categories = categoryRepository.seedDefaultCategories()
            val categoryName = if (debt.direction == DebtDirection.GIVEN) "Borç Geri Alma" else "Borç/Taksit"
            val categoryId = categories.find { it.name == categoryName }?.id ?: return@launch

            debtRepository.update(debt.copy(isPaid = true, paidDate = LocalDate.now()))

            val transaction = Transaction(
                amount = debt.amount,
                type = if (debt.direction == DebtDirection.GIVEN) TransactionType.INCOME else TransactionType.EXPENSE,
                categoryId = categoryId,
                date = LocalDateTime.now(),
                note = if (debt.direction == DebtDirection.GIVEN) {
                    "${debt.personName} adlı kişiden borç geri alındı"
                } else {
                    "${debt.personName} adlı kişiye borç geri ödendi"
                },
                paymentMethod = null,
                currencyCode = "TRY"
            )
            transactionRepository.insert(transaction)
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.delete(debt)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return DebtViewModel(
                    app.container.debtRepository,
                    app.container.transactionRepository,
                    app.container.categoryRepository
                ) as T
            }
        }
    }
}

data class DebtState(
    val debts: List<Debt> = emptyList(),
    val totalGiven: Double = 0.0,
    val totalReceived: Double = 0.0
)
