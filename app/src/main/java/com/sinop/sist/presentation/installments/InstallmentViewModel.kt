package com.sinop.sist.presentation.installments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sinop.sist.SistApplication
import com.sinop.sist.domain.model.Installment
import com.sinop.sist.domain.model.Transaction
import com.sinop.sist.domain.model.TransactionType
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.InstallmentRepository
import com.sinop.sist.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class InstallmentViewModel(
    private val installmentRepository: InstallmentRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InstallmentState())
    val state: StateFlow<InstallmentState> = _state.asStateFlow()

    init {
        loadInstallments()
    }

    private fun loadInstallments() {
        installmentRepository.getAll().onEach { installments ->
            val totalMonthly = installments.filter { !it.isCompleted }.sumOf { it.monthlyAmount }
            _state.value = _state.value.copy(
                installments = installments,
                totalMonthly = totalMonthly
            )
        }.launchIn(viewModelScope)
    }

    fun addInstallment(
        title: String,
        totalAmount: Double,
        installmentCount: Int,
        monthlyAmount: Double,
        startDate: LocalDate,
        cardOrAccount: String?,
        note: String?
    ) {
        viewModelScope.launch {
            installmentRepository.insert(
                Installment(
                    title = title,
                    totalAmount = totalAmount,
                    installmentCount = installmentCount,
                    monthlyAmount = monthlyAmount,
                    startDate = startDate,
                    cardOrAccount = cardOrAccount,
                    remainingCount = installmentCount,
                    note = note
                )
            )
        }
    }

    fun updateInstallment(installment: Installment) {
        viewModelScope.launch {
            installmentRepository.update(installment)
        }
    }

    fun payInstallment(installment: Installment) {
        viewModelScope.launch {
            val categories = categoryRepository.seedDefaultCategories()
            val categoryId = categories.find { it.name == "Borç/Taksit" }?.id ?: return@launch

            val newRemaining = installment.remainingCount - 1
            installmentRepository.update(
                installment.copy(
                    remainingCount = newRemaining,
                    isCompleted = newRemaining <= 0
                )
            )

            val transaction = Transaction(
                amount = installment.monthlyAmount,
                type = TransactionType.EXPENSE,
                categoryId = categoryId,
                date = LocalDateTime.now(),
                note = "${installment.title} taksit ödemesi${installment.cardOrAccount?.let { " - $it" } ?: ""}",
                paymentMethod = null,
                currencyCode = "TRY"
            )
            transactionRepository.insert(transaction)
        }
    }

    fun deleteInstallment(installment: Installment) {
        viewModelScope.launch {
            installmentRepository.delete(installment)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = SistApplication.instance
                    ?: throw IllegalStateException("Application not initialized")
                return InstallmentViewModel(
                    app.container.installmentRepository,
                    app.container.transactionRepository,
                    app.container.categoryRepository
                ) as T
            }
        }
    }
}

data class InstallmentState(
    val installments: List<Installment> = emptyList(),
    val totalMonthly: Double = 0.0
)
